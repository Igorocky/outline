package org.igye.outline.controllers;

import org.igye.outline.model.LoginData;
import org.igye.outline.model.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ControllerUI {

    public static final String LOGIN_DATA = "loginData";

    @Autowired
    private SessionData sessionData;

    @GetMapping("paragraphs")
    public String hello() {
        return "paragraphs";
    }

    @GetMapping("login")
    public String login(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "login";
    }

    @PostMapping("login")
    public String loginPost(Model model, LoginData loginData) {
        if ("igor".equals(loginData.getLogin()) && "pwd".equals(loginData.getPassword())) {
            sessionData.setLogin("igor");
            return "redirect:/home";
        } else {
            sessionData.setLogin(null);
            model.addAttribute(LOGIN_DATA, loginData);
            return "login";
        }
    }

    @GetMapping("home")
    public String home() {
        return "home";
    }

}
