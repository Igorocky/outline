package org.igye.outline.data;

import org.hibernate.Session;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Role;
import org.igye.outline.model.User;
import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.igye.outline.common.OutlineUtils.hashPwd;
import static org.igye.outline.model.Paragraph.ROOT_NAME;

@Component
public class UserDao {
    public static final String ADMIN_ROLE_NAME = "ADMIN";
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<User> loadUsers(User requestor) {
        if (!isAdmin(requestor)) {
            return OutlineUtils.accessDenied();
        } else {
            return OutlineUtils.getCurrentSession(entityManager).createQuery("from User", User.class).getResultList();
        }
    }

    @Transactional
    public User loadUser(User user, UUID id) {
        if (!isAdmin(user)) {
            return OutlineUtils.accessDenied();
        } else {
            return OutlineUtils.getCurrentSession(entityManager).get(User.class, id);
        }
    }

    @Transactional
    public void createUser(User admin, String name, String password, Set<UUID> roles) {
        if (!isAdmin(admin)) {
            OutlineUtils.accessDenied();
        } else {
            Session session = OutlineUtils.getCurrentSession(entityManager);
            User newUser = new User();
            newUser.setName(name);
            newUser.setPassword(hashPwd(password));
            newUser.setRoles(
                    loadRoles().stream().filter(
                            role -> roles.contains(role.getId())
                    ).collect(Collectors.toSet())
            );
            session.persist(newUser);

            Paragraph rootParagraph = new Paragraph();
            rootParagraph.setName(ROOT_NAME);
            rootParagraph.setOwner(newUser);
            session.persist(rootParagraph);
        }
    }

    @Transactional
    public void updateUser(User admin, UUID id, Consumer<User> updates) {
        if (!isAdmin(admin)) {
            OutlineUtils.accessDenied();
        } else {
            User user = loadUser(admin, id);
            updates.accept(user);
        }
    }

    @Transactional
    public void removeUser(User requestor, UUID userIdToRemove) {
        if (!isAdmin(requestor)) {
            OutlineUtils.accessDenied();
        } else if (userIdToRemove.equals(requestor.getId())) {
            throw new OutlineException("userIdToRemove.equals(requestor.getId())");
        } else {
            Session session = OutlineUtils.getCurrentSession(entityManager);
            User userToBeRemoved = session.load(User.class, userIdToRemove);
            List<Paragraph> topParagraphs = session.createQuery(
                    "from Paragraph where owner = :owner and parentParagraph is null",
                    Paragraph.class
            )
                    .setParameter("owner", userToBeRemoved)
                    .getResultList();
            topParagraphs.forEach(par -> session.delete(par));
            session.delete(userToBeRemoved);
        }
    }

    @Transactional
    public List<Role> loadRoles() {
        return OutlineUtils.getCurrentSession(entityManager).createQuery("from Role", Role.class).getResultList();
    }

    private Role loadRole(String name) {
        return OutlineUtils.getCurrentSession(entityManager).createQuery(
                "from Role r where r.name = :name",
                Role.class
        )
                .setParameter("name", name)
                .getSingleResult();
    }

    public boolean isAdmin(User user) {
        for (Role role : user.getRoles()) {
            if (UserDao.ADMIN_ROLE_NAME.equals(role.getName())) {
                return true;
            }
        }
        return false;
    }
}
