package org.igye.outline.controllers;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.NodeDao;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.EditParagraphForm;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.modelv2.ImageV2;
import org.igye.outline.modelv2.ParagraphV2;
import org.igye.outline.modelv2.TopicV2;
import org.igye.outline.selection.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.getImgFile;
import static org.igye.outline.common.OutlineUtils.redirect;

@Controller
@RequestMapping(NodeController.PREFIX)
public class NodeController {
    protected static final String PREFIX = "v2";

    private static final String MIGRATE_DATA = "migrate-data";
    private static final String PARAGRAPH = "paragraph";
    private static final String EDIT_PARAGRAPH = "editParagraph";
    private static final String TOPIC = "topic";

    @Value("${homeUrl}")
    private String homeUrl;
    @Value("${topic.images.location}")
    private String imagesLocation;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private CommonModelMethods commonModelMethods;
    @Autowired
    private NodeDao nodeDao;



    @GetMapping(MIGRATE_DATA)
    public String migrateData(Model model) {
        commonModelMethods.initModel(model);
        nodeDao.migrateData();
        return prefix(homeUrl);
    }

    @GetMapping("home")
    public String home(Model model) {
        commonModelMethods.initModel(model);
        return redirect(prefix(PARAGRAPH));
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
        ParagraphV2 paragraph;
        if (id.isPresent()) {
            paragraph = nodeDao.getParagraphById(id.get());

        } else {
            paragraph = new ParagraphV2();
            paragraph.setName("root");
            paragraph.setChildNodes(nodeDao.getRootNodes());
        }
        model.addAttribute("paragraph", paragraph);
        model.addAttribute("hasWhatToPaste", sessionData.getSelection() != null);
        addPath(model, (ParagraphV2) paragraph.getParentNode());

        return prefix(PARAGRAPH);
    }

    @GetMapping(EDIT_PARAGRAPH)
    public String editParagraph(Model model,
                                @RequestParam Optional<UUID> parentId, @RequestParam Optional<UUID> id) {
        EditParagraphForm form = new EditParagraphForm();
        parentId.ifPresent(parId -> form.setParentId(parId));
        if (id.isPresent()) {
            ParagraphV2 paragraph = nodeDao.getParagraphById(id.get());
            form.setId(paragraph.getId());
            form.setName(paragraph.getName());
        }
        prepareModelForEditParagraph(model, form);
        return prefix(EDIT_PARAGRAPH);
    }

    @PostMapping(EDIT_PARAGRAPH)
    public String editParagraphPost(Model model, EditParagraphForm form,
                                    HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(form.getName())) {
            prepareModelForEditParagraph(model, form);
            return redirect(prefix(EDIT_PARAGRAPH));
        } else {
            UUID idToRedirectTo;
            if (form.getId() != null) {
                nodeDao.updateParagraph(form.getId(), par -> par.setName(form.getName()));
                idToRedirectTo = form.getId();
            } else {
                idToRedirectTo = nodeDao.createParagraph(form.getParentId(), form.getName()).getId();
            }
            return OutlineUtils.redirect(
                    response,
                    prefix(PARAGRAPH),
                    ImmutableMap.of("id", idToRedirectTo)
            );
        }
    }

    private void prepareModelForEditParagraph(Model model, EditParagraphForm form) {
        commonModelMethods.initModel(model);
        if (form.getParentId() != null) {
            addPath(model, nodeDao.getParagraphById(form.getParentId()));
        } else {
            addPath(model, null);
        }
        model.addAttribute("form", form);
    }

    @GetMapping(TOPIC)
    public String topic(Model model, @RequestParam UUID id, Optional<Boolean> showContent,
                        Optional<Boolean> isLeftmostSibling, Optional<Boolean> isRightmostSibling) {
        commonModelMethods.initModel(model);
        TopicV2 topic = nodeDao.getTopicById(id);
        model.addAttribute("topic", topic);
        if (isLeftmostSibling.orElse(false)) {
            model.addAttribute("isLeftmostSibling", true);
        }
        if (isRightmostSibling.orElse(false)) {
            model.addAttribute("isRightmostSibling", true);
        }
        addPath(model, (ParagraphV2) topic.getParentNode());
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
        ImageV2 image = nodeDao.getImageById(imgId);
        try {
            return FileUtils.readFileToByteArray(getImgFile(imagesLocation, image.getId()));
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }

    private String prefix(String url) {
        return "" + PREFIX + "/" + url;
    }

    private void addPath(Model model, ParagraphV2 paragraph) {
        List<ParagraphV2> path = buildPath(paragraph);
        Collections.reverse(path);
        model.addAttribute("path", path);
    }

    private List<ParagraphV2> buildPath(ParagraphV2 paragraph) {
        if (paragraph == null) {
            return new ArrayList<>();
        } else {
            List<ParagraphV2> res = new ArrayList<>();
            res.add(paragraph);
            res.addAll(buildPath((ParagraphV2) paragraph.getParentNode()));
            return res;
        }
    }
}
