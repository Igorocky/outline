package org.igye.outline.controllers;

import org.igye.outline.model.LoginData;
import org.igye.outline.model.SessionData;
import org.igye.outline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
public class ControllerUI {

    public static final String LOGIN_DATA = "loginData";

    @Autowired
    private SessionData sessionData;
    @Autowired
    private Authenticator authenticator;

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
    @Transactional
    public String loginPost(Model model, LoginData loginData) {
        Optional<User> userOptional = authenticator.authenticate(loginData.getLogin(), loginData.getPassword());
        if (userOptional.isPresent()) {
            sessionData.setUser(userOptional.get());
            return "redirect:/home";
        } else {
            sessionData.setUser(null);
            model.addAttribute(LOGIN_DATA, loginData);
            return "login";
        }
    }

    @RequestMapping("logout")
    @Transactional
    public String logout() {
        sessionData.setUser(null);
        return "redirect:/login";
    }

    @GetMapping("home")
    public String home() {
        return "home";
    }

}
