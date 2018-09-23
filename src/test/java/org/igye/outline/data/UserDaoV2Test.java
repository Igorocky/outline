package org.igye.outline.data;

import com.google.common.collect.ImmutableSet;
import org.igye.outline.AbstractHibernateTest;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.exceptions.AccessDeniedException;
import org.igye.outline.modelv2.RoleV2;
import org.igye.outline.modelv2.UserV2;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.common.OutlineUtils.mapToSet;
import static org.igye.outline.data.UserDao.ADMIN_ROLE_NAME;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserDaoV2.class, DaoUtils.class})
public class UserDaoV2Test extends AbstractHibernateTest {
    public static final String USER = "user";
    public static final String TO_BE_CHANGED = "toBeChanged";
    @Autowired
    private UserDaoV2 dao;
    @Autowired
    private UserRepository userRepository;

    @Test(expected = AccessDeniedException.class)
    public void loadUsers_should_throw_an_AccessDeniedException_for_a_user_who_is_not_an_admin() {
        //given
        prepareTestData(b -> b
                .user("admin").admin()
                .user("user1").currentUser()
                .user("user2")
        );

        //when
        List<UserV2> users = dao.loadUsers();

        //then
        //exception should be thrown
    }

    @Test
    public void loadUsers_should_return_nonempty_list_for_a_user_who_is_an_admin() {
        //given
        prepareTestData(b -> b
                .user("admin").admin().currentUser()
                .user("user1")
                .user("user2")
        );

        //when
        List<UserV2> users = dao.loadUsers();

        //then
        Set<String> names = mapToSet(users, UserV2::getName);
        Assert.assertEquals(ImmutableSet.of("admin","user1","user2"), names);
    }

    @Test
    public void loadUsers_should_fetch_roles_for_each_user() {
        //given
        prepareTestData(b -> b
                .user("admin").admin().role("role1").currentUser()
                .user("user1").role("role2")
                .user("user2")
        );

        //when
        List<UserV2> users = dao.loadUsers();

        //then
        Set<String> roles = users.stream()
                .flatMap(u -> u.getRoles().stream().map(r -> r.getName()))
                .collect(Collectors.toSet());
        Assert.assertEquals(ImmutableSet.of(ADMIN_ROLE_NAME,"role1","role2"), roles);
    }

    @Test
    public void loadUsers_should_not_duplicate_users_when_each_user_has_more_than_1_roles() {
        //given
        prepareTestData(b -> b
                .user("admin").admin().role("role1").currentUser()
                .user("user1").role("role2").role("role3")
        );

        //when
        List<UserV2> users = dao.loadUsers();

        //then
        assertEquals(2, users.size());
        Set<String> names = mapToSet(users, UserV2::getName);
        Assert.assertEquals(ImmutableSet.of("admin", "user1"), names);
    }

    @Test
    public void createUser_should_create_new_user() {
        //given
        Map<String, Object> savedData = prepareTestData(b -> b
                .user("admin").admin().role("role1").role("role2").role("role3").currentUser().save(USER)
        );
        Set<UUID> roles = ((UserV2)savedData.get(USER)).getRoles()
                .stream()
                .filter(r -> !r.getName().equals("role2") && !r.getName().equals("ADMIN"))
                .map(RoleV2::getId)
                .collect(Collectors.toSet());

        //when
        dao.createUser("userToBeSaved", "dddd", roles);

        //then
        UserV2 newUser = userRepository.findByName("userToBeSaved");
        Assert.assertEquals("userToBeSaved", newUser.getName());
        Assert.assertTrue(OutlineUtils.checkPwd("dddd", newUser.getPassword()));
        Assert.assertEquals(
                ImmutableSet.of("role1", "role3"),
                map(newUser.getRoles(), RoleV2::getName)
        );
    }

    @Test
    public void updateUser_should_update_existing_user() {
        //given
        Map<String, Object> savedData = prepareTestData(b -> b
                .user("admin").admin().currentUser()
                .user("toBeChanged").save(TO_BE_CHANGED)
        );
        UserV2 userToBeChanged = (UserV2)savedData.get(TO_BE_CHANGED);

        //when
        dao.updateUser(userToBeChanged.getId(), user -> user.setName("NeWNaMe"));

        //then
        UserV2 user = userRepository.findById(userToBeChanged.getId()).get();
        Assert.assertEquals("NeWNaMe", user.getName());
    }

    @Test
    public void it_should_be_impossible_for_a_user_to_lock_himself() {
        //given
        Map<String, Object> savedData = prepareTestData(b -> b
                .user("admin").admin().currentUser().save(USER)
        );
        UserV2 userToBeChanged = (UserV2)savedData.get(USER);

        //when
        dao.updateUser(userToBeChanged.getId(), user -> user.setLocked(true));

        //then
        UserV2 user = userRepository.findById(userToBeChanged.getId()).get();
        Assert.assertFalse(user.getLocked());
    }

    @Test
    public void it_should_be_possible_for_an_admin_to_lock_another_user() {
        //given
        Map<String, Object> savedData = prepareTestData(b -> b
                .user("admin").admin().currentUser()
                .user("user1").save(USER)
        );
        UserV2 userToBeChanged = (UserV2)savedData.get(USER);

        //when
        dao.updateUser(userToBeChanged.getId(), user -> user.setLocked(true));

        //then
        UserV2 user = userRepository.findById(userToBeChanged.getId()).get();
        Assert.assertTrue(user.getLocked());
    }

    @Test
    public void loadRoles_should_load_roles_by_id() {
        //given
        Map<String, Object> savedData = prepareTestData(b -> b
                .user("admin").admin().role("role1").role("role2").role("role3").role("role4").currentUser().save(USER)
        );
        Set<UUID> roleIds = ((UserV2)savedData.get(USER)).getRoles()
                .stream()
                .filter(r -> r.getName().equals("role1") || r.getName().equals("role4"))
                .map(RoleV2::getId)
                .collect(Collectors.toSet());

        //when
        List<RoleV2> result = dao.loadRoles(roleIds);

        //then
        Assert.assertEquals(ImmutableSet.of("role1", "role4"), mapToSet(result, RoleV2::getName));
    }
}