package org.igye.outline.controllers;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.User;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

public class Authenticator {
    public static int BCRYPT_SALT_ROUNDS = 10;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Optional<User> authenticate(String login, String password) {
        Session session = OutlineUtils.getCurrentSession(entityManager);
        List<User> users = session.createQuery("from User where name = :login", User.class)
                .setParameter("login", login)
                .getResultList();
        if (users.isEmpty()) {
            return Optional.empty();
        } else if (users.size() > 1) {
            throw new OutlineException("users.size() > 1");
        } else if (!OutlineUtils.checkPwd(password, users.get(0).getPassword())) {
            return Optional.empty();
        } else {
            User user = users.get(0);
            Hibernate.initialize(user.getRoles());
            return Optional.of(user);
        }
    }

    @Transactional
    public boolean changePassword(User userNp, String oldPassword, String newPassword) {
        if (BCrypt.checkpw(oldPassword, userNp.getPassword())) {
            Session session = OutlineUtils.getCurrentSession(entityManager);
            User user = (User) session.merge(userNp);
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_SALT_ROUNDS)));
            return true;
        } else {
            return false;
        }
    }
}
