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
import org.igye.outline.htmlforms.ContentForForm;
import org.igye.outline.htmlforms.EditParagraphForm;
import org.igye.outline.htmlforms.EditSynopsisTopicForm;
import org.igye.outline.htmlforms.ReorderParagraphChildren;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.Image;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.SynopsisTopic;
import org.igye.outline.model.Text;
import org.igye.outline.model.Topic;
import org.igye.outline.selection.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.igye.outline.common.OutlineUtils.NOTHING;
import static org.igye.outline.common.OutlineUtils.getCurrentUser;
import static org.igye.outline.common.OutlineUtils.getImgFile;
import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.IMAGE;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.TEXT;

@Controller
public class ControllerUI {
    private static final Logger LOG = LogManager.getLogger(ControllerUI.class);

    private static final String HOME = "home";
    private static final String PARAGRAPH = "paragraph";
    private static final String TOPIC = "topic";
    private static final String NEXT_TOPIC = "nextTopic";
    private static final String PREV_TOPIC = "prevTopic";
    private static final String CHANGE_PASSWORD = "changePassword";
    private static final String EDIT_USER = "editUser";
    private static final String EDIT_PARAGRAPH = "editParagraph";
    private static final String REMOVE_USER = "removeUser";
    private static final String LOGIN = "login";
    private static final String USERS = "users";
    private static final String SYNOPSIS = "synopsis";
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
    @Autowired
    private CommonModelMethods commonModelMethods;

    private ObjectMapper mapper = new ObjectMapper();

    private void prepareModelForEditParagraph(Model model, EditParagraphForm form) {
        OutlineUtils.assertNotNull(form.getIdToRedirectToIfCancelled());
        commonModelMethods.initModel(model);
        addPath(model, dao.loadParagraphById(Optional.of(form.getIdToRedirectToIfCancelled()), getCurrentUser()));
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
            Topic topic = dao.loadTopicById(id.get(), getCurrentUser());
            if (topic instanceof SynopsisTopic) {
                return editSynopsisTopic(
                        model,
                        parentId,
                        Optional.of((SynopsisTopic) dao.loadSynopsisTopicByIdWithContent(
                                topic.getId(),
                                getCurrentUser()
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
            addPath(model, dao.loadParagraphById(Optional.of(parId), getCurrentUser()));
        });
        topicOpt.ifPresent(topic -> {
            form.setId(topic.getId());
            form.setName(topic.getName());
            form.setContent(map(
                    topic.getContents(),
                    content -> {
                        if (content instanceof Image) {
                            return ContentForForm.builder().type(IMAGE).id(content.getId()).build();
                        } else if (content instanceof Text) {
                            return ContentForForm.builder().type(TEXT).id(content.getId())
                                    .text(((Text)content).getText()).build();
                        } else {
                            throw new OutlineException("Can't determine type of content.");
                        }
                    }
            ));
            addPath(model, dao.loadParagraphById(Optional.of(topic.getParagraph().getId()), getCurrentUser()));
        });
        commonModelMethods.initModel(model);
        model.addAttribute("form", form);
        model.addAttribute("formDataJson", mapper.writeValueAsString(form));
        return "editSynopsisTopic";
    }

    @PostMapping("editSynopsisTopicPost")
    @ResponseBody
    public UUID editSynopsisTopicPost(@RequestBody EditSynopsisTopicForm form) throws OperationNotSupportedException {
        if (form.getParentId() != null && form.getId() == null) {
            return dao.createSynopsisTopic(getCurrentUser(), form);
        } else if (form.getParentId() == null && form.getId() != null) {
            dao.updateSynopsisTopic(getCurrentUser(), form);
            return form.getId();
        } else {
            throw new OperationNotSupportedException("editSynopsisTopicPost");
        }
    }

    @PostMapping("uploadImage")
    @ResponseBody
    public UUID uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        UUID imgId = dao.createImage(getCurrentUser());
        File imgFile = getImgFile(imagesLocation, imgId);
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
            Paragraph paragraph = dao.loadParagraphById(id, getCurrentUser());
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
                dao.updateParagraph(getCurrentUser(), form.getId(), par -> par.setName(form.getName()));
            } else {
                dao.createParagraph(form.getParentId(), form.getName());
            }
            return OutlineUtils.redirect(response, PARAGRAPH, ImmutableMap.of("id", form.getIdToRedirectToIfCancelled()));
        }
    }

    @PostMapping("reorderParagraphChildren")
    public String reorderParagraphChildren(@RequestBody ReorderParagraphChildren request,
                                  HttpServletResponse response) throws IOException {
        dao.reorderParagraphChildren(getCurrentUser(), request);
        return OutlineUtils.redirect(response, PARAGRAPH, ImmutableMap.of("id", request.getParentId()));
    }

    @PostMapping(REMOVE_USER)
    public String removeUser(@RequestParam UUID id) {
        userDao.removeUser(getCurrentUser(), id);
        return redirect(USERS);
    }

    @GetMapping(HOME)
    public String home(Model model) {
        commonModelMethods.initModel(model);
        return redirect(PARAGRAPH);
    }

    @PostMapping("select")
    public String select(@RequestBody Selection request) {
        sessionData.setSelection(request);
        return NOTHING;
    }

    @PostMapping("performActionOnSelectedObjects")
    public String performActionOnSelectedObjects(@RequestBody UUID destId) {
        dao.performActionOnSelectedObjects(getCurrentUser(), sessionData.getSelection(), destId);
        sessionData.setSelection(null);
        return NOTHING;
    }

    @GetMapping(PARAGRAPH)
    public String paragraph(Model model, @RequestParam Optional<UUID> id, Optional<Boolean> showContent,
                            Optional<Boolean> isLeftmostSibling, Optional<Boolean> isRightmostSibling) {
        commonModelMethods.initModel(model);
        if (isLeftmostSibling.orElse(false)) {
            model.addAttribute("isLeftmostSibling", true);
        }
        if (isRightmostSibling.orElse(false)) {
            model.addAttribute("isRightmostSibling", true);
        }
        if (showContent.orElse(true)) {
            model.addAttribute("showContent", true);
        }
        Paragraph paragraph = dao.loadParagraphById(id, getCurrentUser());
        model.addAttribute("paragraph", paragraph);
        model.addAttribute("hasWhatToPaste", sessionData.getSelection() != null);
        addPath(model, paragraph.getParentParagraph());
        return PARAGRAPH;
    }

    @GetMapping(TOPIC)
    public String topic(Model model, @RequestParam UUID id, Optional<Boolean> showContent,
                        Optional<Boolean> isLeftmostSibling, Optional<Boolean> isRightmostSibling) {
        commonModelMethods.initModel(model);
        Topic topic = dao.loadSynopsisTopicByIdWithContent(id, getCurrentUser());
        model.addAttribute("topic", topic);
        if (isLeftmostSibling.orElse(false)) {
            model.addAttribute("isLeftmostSibling", true);
        }
        if (isRightmostSibling.orElse(false)) {
            model.addAttribute("isRightmostSibling", true);
        }
        addPath(model, topic.getParagraph());
        showContent.ifPresent(b -> model.addAttribute("showContent", b));
        model.addAttribute("hasWhatToPaste", sessionData.getSelection() != null);

        return TOPIC;
    }

    @GetMapping(NEXT_TOPIC)
    public String nextTopic(Model model, @RequestParam UUID id) {
        commonModelMethods.initModel(model);
        Optional<Topic> nextTopicOpt = dao.nextTopic(id, getCurrentUser());
        String redirectUri;
        if (nextTopicOpt.isPresent()) {
            model.addAttribute("topic", nextTopicOpt.get());
            redirectUri = UriComponentsBuilder.newInstance()
                    .path(TOPIC)
                    .queryParam("id", nextTopicOpt.get().getId())
                    .toUriString();
        } else {
            model.addAttribute("isLastTopic", true);
            Topic topic = dao.loadTopicById(id, getCurrentUser());
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
        commonModelMethods.initModel(model);
        Optional<Topic> prevTopicOpt = dao.prevTopic(id, getCurrentUser());
        String redirectUri;
        if (prevTopicOpt.isPresent()) {
            model.addAttribute("topic", prevTopicOpt.get());
            redirectUri = UriComponentsBuilder.newInstance()
                    .path(TOPIC)
                    .queryParam("id", prevTopicOpt.get().getId())
                    .toUriString();
        } else {
            model.addAttribute("isFirstTopic", true);
            Topic topic = dao.loadTopicById(id, getCurrentUser());
            model.addAttribute("topic", topic);
            redirectUri = UriComponentsBuilder.newInstance()
                    .path(TOPIC)
                    .queryParam("id", topic.getId())
                    .queryParam("checkPrev", true)
                    .toUriString();

        }

        return redirect(redirectUri);
    }

    @GetMapping("firstChild")
    public String firstChild(@RequestParam UUID id) {
        return getNode(
                id,
                () -> dao.firstChild(getCurrentUser(), id),
                uri -> {},
                uri -> {}
        );
    }

    @GetMapping("parent")
    public String parent(@RequestParam UUID id) {
        return getNode(
                id,
                () -> dao.loadParent(getCurrentUser(), id),
                uri -> {},
                uri -> {}
        );
    }

    @GetMapping("nextSibling")
    public String nextSibling(@RequestParam UUID id, @RequestParam boolean toTheRight) {
        return getNode(
                id,
                () -> dao.nextSibling(getCurrentUser(), id, toTheRight),
                uri -> {},
                uri -> uri.queryParam(toTheRight ? "isRightmostSibling" : "isLeftmostSibling", true)
        );
    }

    @GetMapping("furthestSibling")
    public String furthestSibling(@RequestParam UUID id, @RequestParam boolean toTheRight) {
        return getNode(
                id,
                () -> dao.furthestSibling(getCurrentUser(), id, toTheRight),
                uri -> uri.queryParam(toTheRight ? "isRightmostSibling" : "isLeftmostSibling", true),
                uri -> uri.queryParam(toTheRight ? "isRightmostSibling" : "isLeftmostSibling", true)
        );
    }

    private String getNode(UUID id, Supplier<Optional<?>> getter,
                           Consumer<UriComponentsBuilder> onPresent,
                           Consumer<UriComponentsBuilder> onAbsent) {
        Optional<?> node = getter.get();
        UriComponentsBuilder redirectUriBuilder;
        if (node.isPresent()) {
            if (node.get() instanceof Topic) {
                redirectUriBuilder = topicUriBuilder((Topic) node.get());
            } else {
                redirectUriBuilder = paragraphUriBuilder((Paragraph) node.get());
            }
            onPresent.accept(redirectUriBuilder);
        } else {
            Object curEntity = dao.loadEntityById(getCurrentUser(), id);
            if (curEntity instanceof Topic) {
                redirectUriBuilder = topicUriBuilder((Topic) curEntity);
            } else {
                redirectUriBuilder = paragraphUriBuilder((Paragraph) curEntity);
            }
            onAbsent.accept(redirectUriBuilder);
        }
        return redirect(redirectUriBuilder.toUriString());
    }

    private UriComponentsBuilder paragraphUriBuilder(Paragraph paragraph) {
        return UriComponentsBuilder.newInstance()
                .path(PARAGRAPH)
                .queryParam("id", paragraph.getId())
                .queryParam("showContent", false)
                ;
    }

    private UriComponentsBuilder topicUriBuilder(Topic topic) {
        return UriComponentsBuilder.newInstance()
                .path(TOPIC)
                .queryParam("id", topic.getId())
                ;
    }

    @GetMapping("topicImage/{imgId}")
    @ResponseBody
    public byte[] topicImage(@PathVariable UUID imgId) {
        Image image = dao.loadImageById(imgId, getCurrentUser());
        try {
            return FileUtils.readFileToByteArray(getImgFile(imagesLocation, image.getId()));
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }

    @GetMapping(USERS)
    public String users(Model model) {
        commonModelMethods.initModel(model);
        model.addAttribute("users", userDao.loadUsers(getCurrentUser()));
        return USERS;
    }

    @GetMapping("version")
    @ResponseBody
    public String version() {
        return version;
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
