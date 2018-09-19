package org.igye.outline.controllers;

import org.igye.outline.data.UserDao;
import org.igye.outline.htmlforms.SessionData;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class CommonModelMethods {

    public void initModel(Model model, SessionData sessionData, UserDao userDao) {
        model.addAttribute("sessionData", sessionData);
        model.addAttribute("isAdmin", userDao.isAdmin(sessionData.getUser()));
    }
}
