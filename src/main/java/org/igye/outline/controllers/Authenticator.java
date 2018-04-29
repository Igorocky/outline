package org.igye.outline.controllers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class Authenticator {
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
        } else if (!password.equals(users.get(0).getPassword())) {
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }

    @Transactional
    public void changePassword(User userNp, String newPassword) {
        Session session = sessionFactory.getCurrentSession();
        User user = (User) session.merge(userNp);
        user.setPassword(newPassword);
    }
}
