package org.igye.outline.data;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline.common.TextProcessing;
import org.igye.outline.data.repository.EngTextRepository;
import org.igye.outline.data.repository.ParagraphRepository;
import org.igye.outline.data.repository.WordRepository;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ChangeAttrValueRequest;
import org.igye.outline.htmlforms.CreateEngTextForm;
import org.igye.outline.htmlforms.CreateWordRequest;
import org.igye.outline.htmlforms.DeleteWordRequest;
import org.igye.outline.htmlforms.EngTextDto;
import org.igye.outline.htmlforms.IgnoreWordRequest;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.htmlforms.WordDto;
import org.igye.outline.model.EngText;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.User;
import org.igye.outline.model.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.igye.outline.common.OutlineUtils.map;

@Component
public class WordsDao {
    public static final String IGNORE_LIST = "(\n)\n";
    @Autowired
    private SessionData sessionData;
    @Autowired
    private ParagraphRepository paragraphRepository;
    @Autowired
    private EngTextRepository engTextRepository;
    @Autowired
    private WordRepository wordRepository;

    @Transactional
    public EngText createEngText(UUID parentId, CreateEngTextForm form) {
        User currUser = sessionData.getCurrentUser();
        EngText engText = new EngText();
        engText.setName(form.getName());
        engText.setOwner(currUser);
        engText.setIgnoreList(IGNORE_LIST);
        Paragraph parent = paragraphRepository.findByOwnerAndId(currUser, parentId);
        if (parent != null) {
            parent.addChildNode(engText);
        } else {
            engTextRepository.save(engText);
        }
        return engText;
    }

    @Transactional
    public EngTextDto getEngTextDtoById(UUID id) {
        EngText text = getEngTextById(id);
        return EngTextDto.builder()
                .textId(text.getId())
                .title(text.getName())
                .text(text.getText())
                .sentences(TextProcessing.splitOnSentences(
                        text.getText(),
                        map(text.getWords(), Word::getWordInText),
                        new LinkedList<>(Arrays.asList(text.getIgnoreList().split("[\r\n]+")))
                ))
                .ignoreList(text.getIgnoreList())
                .learnGroup(text.getLearnGroup())
                .wordsToLearn(map(text.getWords(), this::mapWord))
                .build();
    }

    @Transactional
    public EngText getEngTextById(UUID id) {
        EngText text = engTextRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        if (text == null) {
            throw new OutlineException("Text with id " + id + " was not found.");
        }
        return text;
    }

    @Transactional
    public Word getWordById(UUID id) {
        Word word = wordRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        if (word == null) {
            throw new OutlineException("Word with id " + id + " was not found.");
        }
        return word;
    }

    private WordDto mapWord(Word word) {
        return WordDto.builder()
                .id(word.getId())
                .group(word.getGroup())
                .wordInText(word.getWordInText())
                .word(word.getWord())
                .transcription(word.getTranscription())
                .meaning(word.getMeaning())
                .build();
    }

    @Transactional
    public Object changeAttr(ChangeAttrValueRequest request) {
        if ("eng-text-title".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setName((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-text".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setText((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-ignore-list".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setIgnoreList((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-learn-group".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setLearnGroup((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-group".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setGroup((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-wordInText".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setWordInText((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-spelling".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setWord((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-transcription".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setTranscription((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-meaning".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setMeaning((String) request.getValue());
            return request.getValue();
        } else {
            throw new OutlineException("Unrecognized ChangeAttrValueRequest.attrName: " + request.getAttrName());
        }
    }

    @Transactional
    public WordDto createWord(CreateWordRequest request) {
        EngText text = getEngTextById(request.getEngTextId());
        Word word = Word.builder()
                .id(UUID.randomUUID())
                .group(request.getWord().getGroup())
                .wordInText(request.getWord().getWordInText())
                .word(request.getWord().getWord())
                .transcription(request.getWord().getTranscription())
                .meaning(request.getWord().getMeaning())
                .build();
        text.addWord(word);
        return mapWord(word);
    }

    @Transactional
    public void deleteWord(DeleteWordRequest request) {
        getEngTextById(request.getEngTextId()).detachWordById(request.getWordId());
    }

    @Transactional
    public void ignoreWord(IgnoreWordRequest request) {
        EngText text = getEngTextById(request.getEngTextId());
        String[] arr = StringUtils.split(text.getIgnoreList(), '\n');
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        list.add(request.getSpelling().trim());
        text.setIgnoreList(StringUtils.join(list, '\n'));
    }

    @Transactional
    public void unignoreWord(IgnoreWordRequest request) {
        EngText text = getEngTextById(request.getEngTextId());
        String[] arr = StringUtils.split(text.getIgnoreList(), '\n');
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        while (list.remove(request.getSpelling().trim())) {};
        text.setIgnoreList(StringUtils.join(list, '\n'));
    }

    @Transactional
    public Set<String> listAvailableWordGroups(UUID id) {
        EngText text = getEngTextById(id);
        return text.getWords().stream()
                .map(Word::getGroup)
                .filter(v -> v != null)
                .collect(Collectors.toSet());
    }
}
