package org.igye.outline.controllers;

import org.igye.outline.data.UserDaoV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("v2")
public class UsersController {

    public static final String USERS = "users";

    @Autowired
    private UserDaoV2 userDao;
    @Autowired
    private CommonModelMethods commonModelMethods;


    @GetMapping(USERS)
    public String users(Model model) {
        commonModelMethods.initModel(model);
        model.addAttribute("users", userDao.loadUsers());
        return USERS;
    }


}
