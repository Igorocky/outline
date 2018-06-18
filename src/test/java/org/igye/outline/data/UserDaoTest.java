package org.igye.outline.data;

import com.google.common.collect.ImmutableSet;
import org.igye.outline.AbstractHibernateTest;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.igye.outline.TestUtils.notImplemented;
import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = UserDao.class)
@PropertySource("test.properties")
public class UserDaoTest extends AbstractHibernateTest {
    @Autowired
    private UserDao dao;

    @Test(expected = OutlineException.class)
    public void loadUsers_should_throw_an_exception_for_a_user_who_is_not_an_admin() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("admin").role(UserDao.ADMIN_ROLE_NAME)
                        .user("user1").save()
                        .user("user2")
                        .getResults()
        );
        User user = (User) saved.get(0);

        //when
        List<User> users = dao.loadUsers(user);

        //then
        //exception should be thrown
    }

    @Test
    public void loadUsers_should_return_nonempty_list_for_a_user_who_is_an_admin() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("admin").role(UserDao.ADMIN_ROLE_NAME).save()
                        .user("user1")
                        .user("user2")
                        .getResults()
        );
        User user = (User) saved.get(0);

        //when
        List<User> users = dao.loadUsers(user);

        //then
        Set<String> names = users.stream().map(u -> u.getName()).collect(Collectors.toSet());
        Assert.assertEquals(ImmutableSet.of("admin","user1","user2"), names);
    }

    @Test
    public void loadUsers_should_fetch_roles_for_each_user() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("admin").role(UserDao.ADMIN_ROLE_NAME).role("role1").save()
                        .user("user1").role("role2")
                        .getResults()
        );
        User user = (User) saved.get(0);

        //when
        List<User> users = dao.loadUsers(user);

        //then
        Set<String> roles = users.stream()
                .flatMap(u -> u.getRoles().stream().map(r -> r.getName()))
                .collect(Collectors.toSet());
        Assert.assertEquals(ImmutableSet.of(UserDao.ADMIN_ROLE_NAME,"role1","role2"), roles);
    }

    @Test
    public void loadUsers_should_not_duplicate_users_when_each_user_has_more_than_1_roles() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("admin").role(UserDao.ADMIN_ROLE_NAME).role("role1").save()
                        .user("user1").role("role2").role("role3")
                        .getResults()
        );
        User admin = (User) saved.get(0);

        //when
        List<User> users = dao.loadUsers(admin);

        //then
        assertEquals(2, users.size());
        Set<String> names = users.stream().map(u -> u.getName()).collect(Collectors.toSet());
        Assert.assertEquals(ImmutableSet.of("admin", "user1"), names);
    }

    @Test
    public void createUser_should_create_new_user() {
        //given
        User admin = prepareDataForCreatingNewUser();

        //when
        Set<Long> roles = admin.getRoles().stream()
                .filter(r -> !r.getName().equals(UserDao.ADMIN_ROLE_NAME))
                .map(r -> r.getId())
                .collect(Collectors.toSet());
        dao.createUser(admin, "userToBeSaved", "dddd", roles);

        //then
        User newUser = transactionTemplate.execute(status ->
                getCurrentSession().createQuery("from User where name = :name", User.class)
                        .setParameter("name", "userToBeSaved")
                        .getSingleResult()
        );
        Assert.assertEquals("userToBeSaved", newUser.getName());
        Assert.assertTrue(OutlineUtils.checkPwd("dddd", newUser.getPassword()));
        Assert.assertEquals(roles, newUser.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet()));
    }

    @Test
    public void createUser_should_create_root_paragraph_for_a_new_user() {
        //given
        User admin = prepareDataForCreatingNewUser();

        //when
        dao.createUser(admin, "userToBeSaved", "dddd", ImmutableSet.of());

        //then
        Paragraph paragraph = transactionTemplate.execute(status ->
                getCurrentSession().createQuery("from Paragraph where name = :name", Paragraph.class)
                        .setParameter("name", ROOT_NAME)
                        .getSingleResult()
        );
        Assert.assertEquals("userToBeSaved", paragraph.getOwner().getName());
    }

    @Test
    public void updateUser_should_update_existing_user() {
        //given
        List<User> users = prepareDataForUpdatingExistingUser();
        User admin = users.get(0);
        User userToBeChanged = users.get(1);

        //when
        dao.updateUser(admin, userToBeChanged.getId(), user -> user.setName("NeWNaMe"));

        //then
        User user = transactionTemplate.execute(status -> {
            User u = getCurrentSession().load(User.class, userToBeChanged.getId());
            u.getName();
            return u;
        });
        Assert.assertEquals("NeWNaMe", user.getName());
    }

    @Test
    public void updateUser_should_not_create_root_paragraph_for_existing_user() {
        //given
        List<User> users = prepareDataForUpdatingExistingUser();
        User admin = users.get(0);
        User userToBeChanged = users.get(1);

        //when
        dao.updateUser(admin, userToBeChanged.getId(), user -> {});

        //then
        Integer size = transactionTemplate.execute(status ->
                getCurrentSession().createQuery("from Paragraph where name = :name", Paragraph.class)
                        .setParameter("name", ROOT_NAME)
                        .getResultList().size()
        );
        Assert.assertEquals((Integer) 0, size);
    }

    @Test
    public void removeUser_should_remove_user() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("admin").role(UserDao.ADMIN_ROLE_NAME).save()
                        .user("toBeRemoved").save()
                        .getResults()
        );
        User admin = (User) saved.get(0);
        User userToBeRemoved = (User) saved.get(1);

        //when
        dao.removeUser(admin, userToBeRemoved.getId());

        //then
        Integer size = transactionTemplate.execute(status ->
                getCurrentSession().createQuery("from User", User.class)
                        .getResultList().size()
        );
        Assert.assertEquals((Integer) 1, size);
    }

    @Test
    @Ignore
    public void removeUser_should_remove_all_paragraphs_owned_by_user() {
        notImplemented();
    }

    private User prepareDataForCreatingNewUser() {
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("admin").role(UserDao.ADMIN_ROLE_NAME).role("R1").role("R2").save()
                        .getResults()
        );
        return (User) saved.get(0);
    }

    private List<User> prepareDataForUpdatingExistingUser() {
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("admin").role(UserDao.ADMIN_ROLE_NAME).save()
                        .user("toBeChanged").save()
                        .getResults()
        );
        User admin = (User) saved.get(0);
        User userToBeChanged = (User) saved.get(1);
        userToBeChanged.setName("newName");
        return Arrays.asList(admin, userToBeChanged);
    }
}