package org.igye.outline.controllers;

import org.igye.outline.data.NodeDao;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.modelv2.ParagraphV2;
import org.igye.outline.modelv2.TopicV2;
import org.igye.outline.selection.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.redirect;

@Controller
@RequestMapping(NodeController.PREFIX)
public class NodeController {
    protected static final String PREFIX = "v2";

    private static final String MIGRATE_DATA = "migrate-data";
    private static final String PARAGRAPH = "paragraph";
    private static final String TOPIC = "topic";

    @Value("${homeUrl}")
    private String homeUrl;
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
