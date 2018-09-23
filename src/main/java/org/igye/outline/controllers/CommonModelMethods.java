package org.igye.outline.controllers;

import org.igye.outline.data.UserDao;
import org.igye.outline.htmlforms.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import static org.igye.outline.common.OutlineUtils.getCurrentUser;

@Service
public class CommonModelMethods {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private UserDao userDao;

    public void initModel(Model model) {
        model.addAttribute("sessionData", sessionData);
        model.addAttribute("currentUser", sessionData.getCurrentUser().getName());
        model.addAttribute("isAdmin", userDao.isAdmin(getCurrentUser()));
    }
}
