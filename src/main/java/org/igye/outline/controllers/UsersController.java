package org.igye.outline.controllers;

import org.igye.outline.data.UserDao;
import org.igye.outline.htmlforms.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.igye.outline.common.OutlineUtils.getCurrentUser;

@Controller
@RequestMapping("v2")
public class UsersController {

    public static final String USERS = "users";

    @Autowired
    private SessionData sessionData;
    @Autowired
    private Authenticator authenticator;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CommonModelMethods commonModelMethods;


    @GetMapping(USERS)
    public String users(Model model) {
        commonModelMethods.initModel(model);
        model.addAttribute("users", userDao.loadUsersV2(getCurrentUser()));
        return USERS;
    }


}
