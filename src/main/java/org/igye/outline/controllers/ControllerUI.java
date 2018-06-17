package org.igye.outline.controllers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.igye.outline.data.Dao;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ChangePasswordForm;
import org.igye.outline.htmlforms.LoginForm;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
public class ControllerUI {
    private static final Logger LOG = LogManager.getLogger(ControllerUI.class);

    public static final String HOME = "home";
    public static final String PARAGRAPH = "paragraph";
    public static final String TOPIC = "topic";
    public static final String NEXT_TOPIC = "nextTopic";
    public static final String PREV_TOPIC = "prevTopic";
    public static final String CHANGE_PASSWORD = "changePassword";
    public static final String LOGIN = "login";

    @Value("${topic.images.location}")
    private String topicImagesLocation;

    @Autowired
    private SessionData sessionData;
    @Autowired
    private Authenticator authenticator;
    @Autowired
    private Dao dao;

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
            return redirect(HOME);
        } else {
            sessionData.setUser(null);
            model.addAttribute(LOGIN + "Form", loginForm);
            return LOGIN;
        }
    }

    @RequestMapping("logout")
    public String logout() {
        sessionData.setUser(null);
        return redirect(LOGIN);
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
    public String home(Model model) {
        initModel(model);
        return HOME;
    }

    @GetMapping(PARAGRAPH)
    public String paragraph(Model model, Optional<Long> id) {
        initModel(model);
        Paragraph paragraph = dao.loadParagraphById(id, sessionData.getUser());
        model.addAttribute("paragraph", paragraph);
        addPath(model, paragraph);
        return PARAGRAPH;
    }

    @GetMapping(TOPIC)
    public String topic(Model model, Long id, Optional<Boolean> checkPrev, Optional<Boolean> checkNext,
                        Optional<Boolean> showImages) {
        initModel(model);
        Topic topic = dao.loadTopicById(id, sessionData.getUser());
        model.addAttribute("topic", topic);
        if (checkNext.orElse(false)) {
            Optional<Topic> nextTopicOpt = dao.nextTopic(id, sessionData.getUser());
            if (!nextTopicOpt.isPresent()) {
                model.addAttribute("isLastTopic", true);
            }
        }
        if (checkPrev.orElse(false)) {
            Optional<Topic> prevTopicOpt = dao.prevTopic(id, sessionData.getUser());
            if (!prevTopicOpt.isPresent()) {
                model.addAttribute("isFirstTopic", true);
            }
        }
        addPath(model, topic.getParagraph());
        showImages.map(b -> model.addAttribute("showImages", b));

        return TOPIC;
    }

    @GetMapping(NEXT_TOPIC)
    public String nextTopic(Model model, Long id) {
        initModel(model);
        Optional<Topic> nextTopicOpt = dao.nextTopic(id, sessionData.getUser());
        String redirectUri = null;
        if (nextTopicOpt.isPresent()) {
            model.addAttribute("topic", nextTopicOpt.get());
            redirectUri = UriComponentsBuilder.newInstance()
                    .path(TOPIC)
                    .queryParam("id", nextTopicOpt.get().getId())
                    .toUriString();
        } else {
            model.addAttribute("isLastTopic", true);
            Topic topic = dao.loadTopicById(id, sessionData.getUser());
            model.addAttribute("topic", topic);
            redirectUri = UriComponentsBuilder.newInstance()
                    .path(TOPIC)
                    .queryParam("id", topic.getId())
                    .queryParam("checkNext", true)
                    .toUriString();

        }

        return redirect(redirectUri);
    }

    @GetMapping(PREV_TOPIC)
    public String prevTopic(Model model, Long id) {
        initModel(model);
        Optional<Topic> prevTopicOpt = dao.prevTopic(id, sessionData.getUser());
        String redirectUri = null;
        if (prevTopicOpt.isPresent()) {
            model.addAttribute("topic", prevTopicOpt.get());
            redirectUri = UriComponentsBuilder.newInstance()
                    .path(TOPIC)
                    .queryParam("id", prevTopicOpt.get().getId())
                    .toUriString();
        } else {
            model.addAttribute("isFirstTopic", true);
            Topic topic = dao.loadTopicById(id, sessionData.getUser());
            model.addAttribute("topic", topic);
            redirectUri = UriComponentsBuilder.newInstance()
                    .path(TOPIC)
                    .queryParam("id", topic.getId())
                    .queryParam("checkPrev", true)
                    .toUriString();

        }

        return redirect(redirectUri);
    }

    @GetMapping("topicImg/{topicId}/{imgName}")
    @ResponseBody
    public byte[] topicImg(@PathVariable Long topicId, @PathVariable String imgName) {
        Topic topic = dao.loadTopicById(topicId, sessionData.getUser());
        if (!topic.getImages().contains(imgName)) {
            throw new OutlineException("!topic.getImages().contains(imgName)");
        }
        try {
            return FileUtils.readFileToByteArray(new File(topicImagesLocation + "/" + topicId + "/" + imgName));
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }

    private void initModel(Model model) {
        model.addAttribute("sessionData", sessionData);
    }

    private void addPath(Model model, Paragraph paragraph) {
        List<Paragraph> path = buildPath(paragraph);
        Collections.reverse(path);
        model.addAttribute("path", path);
    }

    private String redirect(String url) {
        return "redirect:/" + url;
    }

    private List<Paragraph> buildPath(Paragraph paragraph) {
        if (paragraph == null || paragraph.getParentParagraph() == null) {
            return new ArrayList<>();
        } else {
            List<Paragraph> res = new ArrayList<>();
            res.add(paragraph);
            res.addAll(buildPath(paragraph.getParentParagraph()));
            return res;
        }
    }
}