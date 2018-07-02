package org.igye.outline.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.Dao;
import org.igye.outline.data.UserDao;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.*;
import org.igye.outline.model.*;
import org.igye.outline.selection.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.igye.outline.common.OutlineUtils.NOTHING;
import static org.igye.outline.common.OutlineUtils.hashPwd;

@Controller
public class ControllerUI {
    private static final Logger LOG = LogManager.getLogger(ControllerUI.class);

    public static final String HOME = "home";
    public static final String PARAGRAPH = "paragraph";
    public static final String TOPIC = "topic";
    public static final String NEXT_TOPIC = "nextTopic";
    public static final String PREV_TOPIC = "prevTopic";
    public static final String CHANGE_PASSWORD = "changePassword";
    public static final String EDIT_USER = "editUser";
    public static final String EDIT_PARAGRAPH = "editParagraph";
    public static final String REMOVE_USER = "removeUser";
    public static final String LOGIN = "login";
    public static final String USERS = "users";
    public static final String SYNOPSIS = "synopsis";
    private static final String CAN_T_DETERMINE_TYPE_OF_TOPIC = "Can't determine type of topic.";

    @Value("${topic.images.location}")
    private String imagesLocation;

    @Value("${app.version}")
    private String version;

    @Autowired
    private SessionData sessionData;
    @Autowired
    private Authenticator authenticator;
    @Autowired
    private Dao dao;
    @Autowired
    private UserDao userDao;

    private ObjectMapper mapper = new ObjectMapper();

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
        return CHANGE_PASSWORD;
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
            return redirect(HOME);
        } else {
            return CHANGE_PASSWORD;
        }
    }

    @GetMapping(EDIT_USER)
    public String editUser(Model model, @RequestParam Optional<UUID> userId) {
        initModel(model);
        model.addAttribute("allRoles", userDao.loadRoles());
        EditUserForm editUserForm = new EditUserForm();
        userId.ifPresent(id -> {
            User user = userDao.loadUser(sessionData.getUser(), id);
            editUserForm.setId(user.getId());
            editUserForm.setName(user.getName());
            editUserForm.getRoles().addAll(user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet()));
        });
        model.addAttribute("editUserForm", editUserForm);
        return EDIT_USER;
    }

    @PostMapping(EDIT_USER)
    public String editUserPost(Model model, EditUserForm editUserForm) {
        model.addAttribute("editUserForm", editUserForm);
        model.addAttribute("allRoles", userDao.loadRoles());
        if (editUserForm.getId() == null) {
            if (!editUserForm.getNewPassword1().equals(editUserForm.getNewPassword2()) ||
                    StringUtils.isEmpty(StringUtils.trim(editUserForm.getNewPassword1()))) {
                return EDIT_USER;
            } else {
                userDao.createUser(
                        sessionData.getUser(),
                        editUserForm.getName(),
                        editUserForm.getNewPassword1(),
                        editUserForm.getRoles()
                );
                return redirect(USERS);
            }
        } else {
            boolean passwordWasChanged = !StringUtils.isEmpty(StringUtils.trim(editUserForm.getNewPassword1()));
            if (passwordWasChanged && !editUserForm.getNewPassword1().equals(editUserForm.getNewPassword2())) {
                return EDIT_USER;
            } else {
                userDao.updateUser(
                        sessionData.getUser(),
                        editUserForm.getId(),
                        user -> {
                            user.setName(editUserForm.getName());
                            if (passwordWasChanged) {
                                user.setPassword(hashPwd(editUserForm.getNewPassword1()));
                            }
                            user.setRoles(
                                    userDao.loadRoles().stream().filter(
                                            role -> editUserForm.getRoles().contains(role.getId())
                                    ).collect(Collectors.toSet())
                            );
                        }
                );
                return redirect(USERS);
            }
        }
    }

    private void prepareModelForEditParagraph(Model model, EditParagraphForm form) {
        OutlineUtils.assertNotNull(form.getIdToRedirectTo());
        initModel(model);
        addPath(model, dao.loadParagraphById(Optional.of(form.getIdToRedirectTo()), sessionData.getUser()));
        model.addAttribute("form", form);
    }

    @GetMapping("editTopic")
    public String editTopic(Model model,
                                @RequestParam Optional<String> topicType,
                                @RequestParam Optional<UUID> parentId, @RequestParam Optional<UUID> id) throws JsonProcessingException {
        if (parentId.isPresent() && !id.isPresent()) {
            if (SYNOPSIS.equals(topicType.get())) {
                return editSynopsisTopic(model, parentId, Optional.empty());
            } else {
                throw new OutlineException(CAN_T_DETERMINE_TYPE_OF_TOPIC);
            }
        } else if (!parentId.isPresent() && id.isPresent()) {
            Topic topic = dao.loadTopicById(id.get(), sessionData.getUser());
            if (topic instanceof SynopsisTopic) {
                return editSynopsisTopic(
                        model,
                        parentId,
                        Optional.of((SynopsisTopic) dao.loadSynopsisTopicByIdWithContent(
                                topic.getId(),
                                sessionData.getUser()
                        ))
                );
            } else {
                throw new OutlineException(CAN_T_DETERMINE_TYPE_OF_TOPIC);
            }
        } else {
            throw new OutlineException(CAN_T_DETERMINE_TYPE_OF_TOPIC);
        }
    }

    protected String editSynopsisTopic(Model model,
                                       Optional<UUID> parentId, Optional<SynopsisTopic> topicOpt) throws JsonProcessingException {
        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        parentId.ifPresent(parId -> {
            form.setParentId(parId);
            addPath(model, dao.loadParagraphById(Optional.of(parId), sessionData.getUser()));
        });
        topicOpt.ifPresent(topic -> {
            form.setId(topic.getId());
            form.setName(topic.getName());
            form.setContent(
                    topic.getContents().stream().map(content -> {
                        if (content instanceof Image) {
                            return ContentForForm.builder().type(ContentForForm.IMAGE).id(content.getId()).build();
                        } else if (content instanceof Text) {
                            return ContentForForm.builder().type(ContentForForm.TEXT).id(content.getId())
                                    .text(((Text)content).getText()).build();
                        } else {
                            throw new OutlineException("Can't determine type of content.");
                        }
                    }).collect(Collectors.toList())
            );
            addPath(model, dao.loadParagraphById(Optional.of(topic.getParagraph().getId()), sessionData.getUser()));
        });
        initModel(model);
        model.addAttribute("form", form);
        model.addAttribute("formDataJson", mapper.writeValueAsString(form));
        return "editSynopsisTopic";
    }

    @PostMapping("editSynopsisTopicPost")
    @ResponseBody
    public UUID editSynopsisTopicPost(Model model, @RequestBody EditSynopsisTopicForm form,
                                    HttpServletResponse response) throws IOException, OperationNotSupportedException {
        if (form.getParentId() != null && form.getId() == null) {
            return dao.createSynopsisTopic(sessionData.getUser(), form);
        } else {
            throw new OperationNotSupportedException("editSynopsisTopicPost");
        }
    }

    @PostMapping("uploadImage")
    @ResponseBody
    public UUID uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        UUID imgId = dao.createImage(sessionData.getUser());
        File imgFile = getImgFile(imgId);
        File parentDir = imgFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(imgFile);
        return imgId;
    }

    @GetMapping(EDIT_PARAGRAPH)
    public String editParagraph(Model model,
                                @RequestParam Optional<UUID> parentId, @RequestParam Optional<UUID> id) {
        EditParagraphForm form = new EditParagraphForm();
        parentId.ifPresent(parId -> form.setParentId(parId));
        id.ifPresent(idd -> form.setId(idd));
        if (id.isPresent()) {
            Paragraph paragraph = dao.loadParagraphById(id, sessionData.getUser());
            form.setId(paragraph.getId());
            form.setName(paragraph.getName());
        }
        prepareModelForEditParagraph(model, form);
        return EDIT_PARAGRAPH;
    }

    @PostMapping(EDIT_PARAGRAPH)
    public String editParagraphPost(Model model, EditParagraphForm form,
                                  HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(form.getName())) {
            prepareModelForEditParagraph(model, form);
            return redirect(EDIT_PARAGRAPH);
        } else {
            if (form.getId() != null) {
                dao.updateParagraph(sessionData.getUser(), form.getId(), par -> par.setName(form.getName()));
            } else {
                dao.createParagraph(form.getParentId(), form.getName());
            }
            return OutlineUtils.redirect(response, PARAGRAPH, ImmutableMap.of("id", form.getIdToRedirectTo()));
        }
    }

    @PostMapping("reorderParagraphChildren")
    public String reorderParagraphChildren(@RequestBody ReorderParagraphChildren request,
                                  HttpServletResponse response) throws IOException {
        dao.reorderParagraphChildren(sessionData.getUser(), request);
        return OutlineUtils.redirect(response, PARAGRAPH, ImmutableMap.of("id", request.getParentId()));
    }

    @PostMapping(REMOVE_USER)
    public String removeUser(Model model, @RequestParam UUID id) {
        userDao.removeUser(sessionData.getUser(), id);
        return redirect(USERS);
    }

    @GetMapping(HOME)
    public String home(Model model) {
        initModel(model);
        return redirect(PARAGRAPH);
    }

    @PostMapping("select")
    public String select(@RequestBody Selection request) {
        sessionData.setSelection(request);
        return NOTHING;
    }

    @PostMapping("performActionOnSelectedObjects")
    public String performActionOnSelectedObjects(@RequestBody UUID destParId) {
        dao.performActionOnSelectedObjects(sessionData.getUser(), sessionData.getSelection(), destParId);
        sessionData.setSelection(null);
        return NOTHING;
    }

    @GetMapping(PARAGRAPH)
    public String paragraph(Model model, @RequestParam Optional<UUID> id) {
        initModel(model);
        Paragraph paragraph = dao.loadParagraphById(id, sessionData.getUser());
        model.addAttribute("paragraph", paragraph);
        model.addAttribute("hasWhatToPaste", sessionData.getSelection() != null);
        addPath(model, paragraph);
        return PARAGRAPH;
    }

    @GetMapping(TOPIC)
    public String topic(Model model, @RequestParam UUID id, Optional<Boolean> checkPrev, Optional<Boolean> checkNext,
                        Optional<Boolean> showImages) {
        initModel(model);
        Topic topic = dao.loadSynopsisTopicByIdWithContent(id, sessionData.getUser());
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
        showImages.ifPresent(b -> model.addAttribute("showImages", b));

        return TOPIC;
    }

    @GetMapping(NEXT_TOPIC)
    public String nextTopic(Model model, @RequestParam UUID id) {
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
    public String prevTopic(Model model, @RequestParam UUID id) {
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

    @GetMapping("topicImage/{imgId}")
    @ResponseBody
    public byte[] topicImage(@PathVariable UUID imgId) {
        Image image = dao.loadImageById(imgId, sessionData.getUser());
        try {
            return FileUtils.readFileToByteArray(getImgFile(image.getId()));
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }

    @GetMapping(USERS)
    public String users(Model model) {
        initModel(model);
        model.addAttribute("users", userDao.loadUsers(sessionData.getUser()));
        return USERS;
    }

    @GetMapping("version")
    @ResponseBody
    public String version() {
        return version;
    }

    private File getImgFile(UUID imgId) {
        String idStr = imgId.toString();
        return new File(imagesLocation + "/" + idStr.substring(0,2) + "/" + idStr);
    }

    private void initModel(Model model) {
        model.addAttribute("sessionData", sessionData);
        model.addAttribute("isAdmin", userDao.isAdmin(sessionData.getUser()));
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
