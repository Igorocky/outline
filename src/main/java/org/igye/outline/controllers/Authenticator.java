package org.igye.outline.controllers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class Authenticator {
    public static int BCRYPT_SALT_ROUNDS = 10;
    @Autowired
    private SessionFactory sessionFactory;

    @Transactional
    public Optional<User> authenticate(String login, String password) {
        Session session = sessionFactory.getCurrentSession();
        List<User> users = session.createQuery("from User where name = :login", User.class)
                .setParameter("login", login)
                .getResultList();
        if (users.isEmpty()) {
            return Optional.empty();
        } else if (users.size() > 1) {
            throw new OutlineException("users.size() > 1");
        } else if (!BCrypt.checkpw(password, users.get(0).getPassword())) {
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }

    @Transactional
    public boolean changePassword(User userNp, String oldPassword, String newPassword) {
        if (BCrypt.checkpw(oldPassword, userNp.getPassword())) {
            Session session = sessionFactory.getCurrentSession();
            User user = (User) session.merge(userNp);
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_SALT_ROUNDS)));
            return true;
        } else {
            return false;
        }
    }
}
