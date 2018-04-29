package org.igye.outline.controllers;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline.htmlforms.ChangePasswordForm;
import org.igye.outline.htmlforms.LoginForm;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
public class ControllerUI {
    public static final String HOME = "home";
    public static final String CHANGE_PASSWORD = "changePassword";
    public static final String LOGIN = "login";
    @Autowired
    private SessionData sessionData;
    @Autowired
    private Authenticator authenticator;

    @GetMapping("paragraphs")
    public String hello() {
        return "paragraphs";
    }

    @GetMapping(LOGIN)
    public String login(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return LOGIN;
    }

    @PostMapping(LOGIN)
    public String loginPost(Model model, LoginForm loginForm) {
        Optional<User> userOptional = authenticator.authenticate(loginForm.getLogin(), loginForm.getPassword());
        if (userOptional.isPresent()) {
            sessionData.setUser(userOptional.get());
            return "redirect:/" + HOME;
        } else {
            sessionData.setUser(null);
            model.addAttribute(LOGIN + "Form", loginForm);
            return LOGIN;
        }
    }

    @RequestMapping("logout")
    public String logout() {
        sessionData.setUser(null);
        return "redirect:/" + LOGIN;
    }

    @GetMapping(CHANGE_PASSWORD)
    public String changePassword(Model model) {
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        model.addAttribute("changePasswordForm", changePasswordForm);
        return "changePassword";
    }

    @PostMapping(CHANGE_PASSWORD)
    public String changePasswordPost(Model model, ChangePasswordForm changePasswordForm) {
        model.addAttribute("changePasswordForm", changePasswordForm);
        if (!changePasswordForm.getNewPassword1().equals(changePasswordForm.getNewPassword2()) ||
                StringUtils.isEmpty(StringUtils.trim(changePasswordForm.getNewPassword1()))) {
            return CHANGE_PASSWORD;
        } else if(authenticator.changePassword(
                sessionData.getUser(), changePasswordForm.getOldPassword(), changePasswordForm.getNewPassword1()
        )) {
            return HOME;
        } else {

            return CHANGE_PASSWORD;
        }
    }

    @GetMapping(HOME)
    public String home() {
        return HOME;
    }

}
