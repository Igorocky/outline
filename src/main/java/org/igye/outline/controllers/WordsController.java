package org.igye.outline.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.WordsDao;
import org.igye.outline.htmlforms.CreateEngTextForm;
import org.igye.outline.htmlforms.CreateWordRequest;
import org.igye.outline.htmlforms.DeleteWordRequest;
import org.igye.outline.htmlforms.IgnoreWordRequest;
import org.igye.outline.model.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.createResponse;
import static org.igye.outline.common.OutlineUtils.createVoidResponse;
import static org.igye.outline.common.OutlineUtils.redirect;

@Controller
@RequestMapping(WordsController.PREFIX)
public class WordsController {
    protected static final String PREFIX = "words/";

    private static final String CREATE_ENG_TEXT = "createEngText";
    private static final String ENG_TEXT = "engText";
    private static final String PREPARE_TEXT = "prepareText";

    @Autowired
    private CommonModelMethods commonModelMethods;
    @Autowired
    private WordsDao wordsDao;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping(CREATE_ENG_TEXT)
    public String createEngText(Model model, @RequestParam Optional<UUID> parentId) {
        CreateEngTextForm form = new CreateEngTextForm();
        parentId.ifPresent(parId -> form.setParentId(parId));
        commonModelMethods.prepareModelForEditNode(model, form);
        return prefix(CREATE_ENG_TEXT);
    }

    @PostMapping(CREATE_ENG_TEXT)
    public String createEngTextPost(Model model, CreateEngTextForm form,
                                    HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(form.getName())) {
            commonModelMethods.prepareModelForEditNode(model, form);
            return redirect(prefix(CREATE_ENG_TEXT));
        } else {
            UUID idToRedirectTo;
            idToRedirectTo = wordsDao.createEngText(form.getParentId(), form).getId();
            return OutlineUtils.redirect(
                    response,
                    prefix(ENG_TEXT),
                    ImmutableMap.of("id", idToRedirectTo)
            );
        }
    }

    @GetMapping(PREPARE_TEXT)
    public String prepareText(Model model, @RequestParam UUID id) {
        commonModelMethods.initModel(model);
        commonModelMethods.addPath(model, (Paragraph) wordsDao.getEngTextById(id).getParentNode());
        model.addAttribute("engTextId", id);
        return prefix(PREPARE_TEXT);
    }

    @GetMapping("engText/{id}")
    @ResponseBody
    public Map<String, Object> prepareText(@PathVariable UUID id) {
        return createResponse("engText", wordsDao.getEngTextDtoById(id));
    }

    @PostMapping("createWord")
    @ResponseBody
    public Map<String, Object> createWord(@RequestBody CreateWordRequest request) {
        return createResponse("word", wordsDao.createWord(request));
    }

    @PostMapping("removeWord")
    @ResponseBody
    public Map<String, Object> createWord(@RequestBody DeleteWordRequest request) {
        wordsDao.deleteWord(request);
        return createVoidResponse();
    }

    @PostMapping("ignoreWord")
    @ResponseBody
    public Map<String, Object> ignoreWord(@RequestBody IgnoreWordRequest request) {
        wordsDao.ignoreWord(request);
        return createVoidResponse();
    }

    @PostMapping("unignoreWord")
    @ResponseBody
    public Map<String, Object> unignoreWord(@RequestBody IgnoreWordRequest request) {
        wordsDao.unignoreWord(request);
        return createVoidResponse();
    }

    @PostMapping("changeLearnGroups/{textId}")
    @ResponseBody
    public Map<String, Object> unignoreWord(@PathVariable UUID textId, @RequestBody List<String> request) {
        wordsDao.changeLearnGroups(textId, request);
        return createVoidResponse();
    }

    @GetMapping("engText/availableWordGroups/{id}")
    @ResponseBody
    public Map<String, Object> availableWordGroups(@PathVariable UUID id) {
        return createResponse("availableWordGroups", wordsDao.listAvailableWordGroups(id));
    }

    @GetMapping("engText/learnGroupsInfo/{id}")
    @ResponseBody
    public Map<String, Object> learnGroupsInfo(@PathVariable UUID id) {
        return wordsDao.getLearnGroupsInfo(id);
    }


    private String prefix(String url) {
        return OutlineUtils.prefix(PREFIX, url);
    }
}
