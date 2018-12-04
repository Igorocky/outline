package org.igye.outline.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.NodeDao;
import org.igye.outline.data.WordsDao;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.export.Exporter;
import org.igye.outline.htmlforms.ChangeAttrValueRequest;
import org.igye.outline.htmlforms.ContentForForm;
import org.igye.outline.htmlforms.EditParagraphForm;
import org.igye.outline.htmlforms.EditTopicForm;
import org.igye.outline.htmlforms.ImageType;
import org.igye.outline.htmlforms.ReorderNodeChildren;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.Image;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Text;
import org.igye.outline.model.Topic;
import org.igye.outline.selection.ObjectType;
import org.igye.outline.selection.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.igye.outline.common.OutlineUtils.NOTHING;
import static org.igye.outline.common.OutlineUtils.createResponse;
import static org.igye.outline.common.OutlineUtils.getIconsInfo;
import static org.igye.outline.common.OutlineUtils.getImgFile;
import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.common.OutlineUtils.redirect;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.IMAGE;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.TEXT;

@Controller
@RequestMapping(NodeController.PREFIX)
public class NodeController {
    protected static final String PREFIX = "";

    private static final String MIGRATE_DATA = "migrate-data";
    private static final String PARAGRAPH = "paragraph";
    private static final String EDIT_PARAGRAPH = "editParagraph";
    private static final String EDIT_TOPIC = "editTopic";
    private static final String TOPIC = "topic";

    @Value("${homeUrl}")
    private String homeUrl;
    @Value("${topic.images.location}")
    private String imagesLocation;
    @Value("${app.version}")
    private String appVersion;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private CommonModelMethods commonModelMethods;
    @Autowired
    private NodeDao nodeDao;
    @Autowired
    private WordsDao wordsDao;
    @Autowired
    private Exporter exporter;

    private ObjectMapper mapper = new ObjectMapper();


    @GetMapping("home")
    public String home(Model model) {
        commonModelMethods.initModel(model);
        return redirect(prefix(PARAGRAPH));
    }

    @GetMapping("/")
    public String home2(Model model) {
        commonModelMethods.initModel(model);
        return redirect(prefix("home"));
    }

    @GetMapping(PARAGRAPH)
    public String paragraph(Model model, @RequestParam Optional<UUID> id, Optional<Boolean> showContent,
                            Optional<Boolean> isLeftmostSibling, Optional<Boolean> isRightmostSibling) throws JsonProcessingException {
        commonModelMethods.initModel(model);

        Paragraph paragraph;
        if (id.isPresent()) {
            paragraph = nodeDao.getParagraphById(id.get());

        } else {
            paragraph = new Paragraph();
            paragraph.setId(null);
            paragraph.setName("root");
            paragraph.setChildNodes(nodeDao.getRootNodes());
        }
        model.addAttribute("paragraph", paragraph);
        model.addAttribute("hasWhatToPaste", sessionData.getSelection() != null);
        commonModelMethods.addPath(model, (Paragraph) paragraph.getParentNode());

        if (isLeftmostSibling.orElse(false)) {
            model.addAttribute("isLeftmostSibling", true);
        }
        if (isRightmostSibling.orElse(false)) {
            model.addAttribute("isRightmostSibling", true);
        }
        if (showContent.orElse(true)) {
            model.addAttribute("showContent", true);
            boolean showIcons = hasAtLeastOneIcon(paragraph);
            model.addAttribute("showIcons", showIcons);
            model.addAttribute(
                    "iconsDataJson",
                    showIcons
                            ? mapper.writeValueAsString(getIconsInfo(paragraph.getChildNodes()))
                            : "[]"
            );
        } else {
            model.addAttribute("showIcons", false);
            model.addAttribute("iconsDataJson", "[]");
        }

        return prefix(PARAGRAPH);
    }

    private boolean hasAtLeastOneIcon(Paragraph paragraph) {
        return paragraph.getChildNodes().stream().anyMatch(
                node ->
                        (node instanceof Topic)
                                ? ((Topic)node).getIcon() != null
                                : (node instanceof Paragraph)
                                  ? ((Paragraph)node).getIcon() != null : false
        );
    }

    @GetMapping(EDIT_PARAGRAPH)
    public String editParagraph(Model model,
                                @RequestParam Optional<UUID> parentId, @RequestParam Optional<UUID> id) {
        EditParagraphForm form = new EditParagraphForm();
        parentId.ifPresent(parId -> form.setParentId(parId));
        if (id.isPresent()) {
            Paragraph paragraph = nodeDao.getParagraphById(id.get());
            form.setId(paragraph.getId());
            form.setName(paragraph.getName());
            form.setIconId(paragraph.getIcon() != null ? paragraph.getIcon().getId() : null);
            form.setSol(paragraph.isSol());
        }
        commonModelMethods.prepareModelForEditNode(model, form);
        return prefix(EDIT_PARAGRAPH);
    }

    @PostMapping(EDIT_PARAGRAPH)
    public String editParagraphPost(Model model, EditParagraphForm form,
                                    HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(form.getName())) {
            commonModelMethods.prepareModelForEditNode(model, form);
            return redirect(prefix(EDIT_PARAGRAPH));
        } else {
            UUID idToRedirectTo;
            if (form.getId() != null) {
                nodeDao.updateParagraph(form);
                idToRedirectTo = form.getId();
            } else {
                idToRedirectTo = nodeDao.createParagraph(form.getParentId(), form).getId();
            }
            return OutlineUtils.redirect(
                    response,
                    prefix(PARAGRAPH),
                    ImmutableMap.of("id", idToRedirectTo)
            );
        }
    }

    @GetMapping(TOPIC)
    public String topic(Model model, @RequestParam UUID id, Optional<Boolean> showContent,
                        Optional<Boolean> isLeftmostSibling, Optional<Boolean> isRightmostSibling) {
        commonModelMethods.initModel(model);
        Topic topic = nodeDao.getTopicById(id);
        model.addAttribute("topic", topic);
        if (isLeftmostSibling.orElse(false)) {
            model.addAttribute("isLeftmostSibling", true);
        }
        if (isRightmostSibling.orElse(false)) {
            model.addAttribute("isRightmostSibling", true);
        }
        commonModelMethods.addPath(model, (Paragraph) topic.getParentNode());
        showContent.ifPresent(b -> model.addAttribute("showContent", b));
        model.addAttribute(
                "hasWhatToPaste",
                sessionData.getSelection() != null &&
                        sessionData.getSelection().getSelections() != null &&
                        sessionData.getSelection().getSelections().stream()
                        .anyMatch(s -> s.getObjectType() == ObjectType.IMAGE)
        );

        return prefix(TOPIC);
    }

    @GetMapping("topicImage/{imgId}")
    @ResponseBody
    public byte[] topicImage(@PathVariable UUID imgId) {
        return getImgFileById(nodeDao.getImageById(imgId).getId());
    }

    @GetMapping("icon/{iconId}")
    @ResponseBody
    public byte[] icon(@PathVariable UUID iconId) {
        return getImgFileById(nodeDao.getIconById(iconId).getId());
    }

    @GetMapping(EDIT_TOPIC)
    public String editTopic(Model model,
                            @RequestParam Optional<UUID> parentId, @RequestParam Optional<UUID> id) throws JsonProcessingException {
        EditTopicForm form = new EditTopicForm();
        parentId.ifPresent(parId -> {
            form.setParentId(parId);
            commonModelMethods.addPath(model, nodeDao.getParagraphById(parId));
        });
        if (id.isPresent()) {
            Topic topic = nodeDao.getTopicById(id.get());
            form.setId(topic.getId());
            form.setName(topic.getName());
            form.setIconId(topic.getIcon() != null ? topic.getIcon().getId() : null);
            form.setSol(topic.isSol());
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
            if (topic.getParentNode() != null) {
                commonModelMethods.addPath(model, (Paragraph) topic.getParentNode());
            }
        }
        commonModelMethods.initModel(model);
        model.addAttribute("form", form);
        model.addAttribute("formDataJson", mapper.writeValueAsString(form));
        return prefix(EDIT_TOPIC);
    }

    @PostMapping(EDIT_TOPIC)
    @ResponseBody
    public UUID editTopicPost(@RequestBody EditTopicForm form) {
        if (form.getId() == null) {
            return nodeDao.createTopic(form);
        } else {
            nodeDao.updateTopic(form);
            return form.getId();
        }
    }

    @PostMapping("uploadImage")
    @ResponseBody
    public UUID uploadImage(@RequestParam("file") MultipartFile file,
                            @RequestParam("imageType") ImageType imageType) throws IOException {
        UUID imgId = imageType == ImageType.TOPIC_IMAGE ? nodeDao.createImage() : nodeDao.createIcon();
        File imgFile = getImgFile(imagesLocation, imgId);
        File parentDir = imgFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(imgFile);
        return imgId;
    }

    @PostMapping("reorderNodeChildren")
    public String reorderNodeChildren(@RequestBody ReorderNodeChildren request,
                                           HttpServletResponse response) throws IOException {
        nodeDao.reorderNodeChildren(request);
        return OutlineUtils.redirect(response, prefix(PARAGRAPH), ImmutableMap.of("id", request.getParentId()));
    }

    @PostMapping("select")
    public String select(@RequestBody Selection request) {
        sessionData.setSelection(request);
        return prefix(NOTHING);
    }

    @PostMapping("performActionOnSelectedObjects")
    public String performActionOnSelectedObjects(@RequestBody Optional<UUID> destId) {
        nodeDao.performActionOnSelectedObjects(sessionData.getSelection(), destId.orElse(null));
        sessionData.setSelection(null);
        return prefix(NOTHING);
    }

    @GetMapping("firstChild")
    public String firstChild(@RequestParam Optional<UUID> id) {
        return getNode(
                id,
                () -> nodeDao.firstChild(id),
                uri -> {},
                uri -> {}
        );
    }

    @GetMapping("parent")
    public String parent(@RequestParam Optional<UUID> id) {
        return getNode(
                id,
                () -> id.flatMap(nodeDao::loadParent),
                uri -> {},
                uri -> {}
        );
    }

    @GetMapping("nextSibling")
    public String nextSibling(@RequestParam Optional<UUID> id, @RequestParam boolean toTheRight) {
        return getNode(
                id,
                () -> id.flatMap(idd -> nodeDao.nextSibling(idd, toTheRight)),
                uri -> {},
                uri -> uri.queryParam(toTheRight ? "isRightmostSibling" : "isLeftmostSibling", true)
        );
    }

    @GetMapping("furthestSibling")
    public String furthestSibling(@RequestParam Optional<UUID> id, @RequestParam boolean toTheRight) {
        return getNode(
                id,
                () -> id.flatMap(idd -> nodeDao.furthestSibling(idd, toTheRight)),
                uri -> uri.queryParam(toTheRight ? "isRightmostSibling" : "isLeftmostSibling", true),
                uri -> uri.queryParam(toTheRight ? "isRightmostSibling" : "isLeftmostSibling", true)
        );
    }

    @GetMapping("export")
    public String export(@RequestParam UUID id) throws IOException, InterruptedException {
        exporter.export(id);
        return prefix(NOTHING);
    }

    @PostMapping("changeAttrValue")
    @ResponseBody
    public Map<String, Object> changeAttrValue(@RequestBody ChangeAttrValueRequest request) {
        if (request.getAttrName().startsWith("eng-text-")) {
            return createResponse("value", wordsDao.changeAttr(request));
        }
        throw new OutlineException("Unrecognized ChangeAttrValueRequest.attrName: " + request.getAttrName());
    }

    @PostMapping("backup")
    @ResponseBody
    public void backup() {
        nodeDao.backup();
    }

    @GetMapping("version")
    @ResponseBody
    public String version() {
        return appVersion + "<br/>https://github.com/Igorocky/outline";
    }

    private String getNode(Optional<UUID> id, Supplier<Optional<?>> getter,
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
        } else if (id.isPresent()) {
            Object curEntity = nodeDao.loadNodeById(id.get());
            if (curEntity instanceof Topic) {
                redirectUriBuilder = topicUriBuilder((Topic) curEntity);
            } else {
                redirectUriBuilder = paragraphUriBuilder((Paragraph) curEntity);
            }
            onAbsent.accept(redirectUriBuilder);
        } else {
            redirectUriBuilder = UriComponentsBuilder.newInstance()
                    .path(prefix(PARAGRAPH))
                    .queryParam("showContent", false);
        }
        return redirect(redirectUriBuilder.toUriString());
    }

    private UriComponentsBuilder paragraphUriBuilder(Paragraph paragraph) {
        return UriComponentsBuilder.newInstance()
                .path(prefix(PARAGRAPH))
                .queryParam("id", paragraph.getId())
                .queryParam("showContent", false)
                ;
    }

    private UriComponentsBuilder topicUriBuilder(Topic topic) {
        return UriComponentsBuilder.newInstance()
                .path(prefix(TOPIC))
                .queryParam("id", topic.getId())
                ;
    }

    private String prefix(String url) {
        return OutlineUtils.prefix(PREFIX, url);
    }

    private byte[] getImgFileById(UUID id) {
        try {
            return FileUtils.readFileToByteArray(getImgFile(imagesLocation, id));
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }
}
