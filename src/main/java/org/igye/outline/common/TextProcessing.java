package org.igye.outline.common;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline.model.EngText;
import org.igye.outline.model.Word;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.igye.outline.common.OutlineUtils.filter;
import static org.igye.outline.common.OutlineUtils.listF;
import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.common.OutlineUtils.mapToSet;

public class TextProcessing {
    /*
       '—' - 2014
       '…' - 2026
       '„' - 201E
       '”' - 201D
      */
    private static final String BORDER_SYMBOL = "[\\.?!\\u2026\\s\\r\\n,:;\"\\(\\)\\[\\]\\\\/\\*\\u201E\\u201D]";
    private static final String SENTENCE_PARTS_DELIMITER = "((?<=" + BORDER_SYMBOL + ")(?!" + BORDER_SYMBOL + "))|((?<!" + BORDER_SYMBOL + ")(?=" + BORDER_SYMBOL + "))";
    private static final Pattern HIDABLE_PATTERN = Pattern.compile("^[\\(\\)-.,\\s–\":\\[\\]\\\\/;!?\\u2014\\u2026\\u201E\\u201D]+$");
    private static final List<String> SENTENCE_ENDS = listF(".", "!", "?", "…");
    private static final List<String> R_N = listF("\r", "\n");
    public static final String ALL_WORDS = "All Words";
    public static final String ALL_GROUPS = "All Groups";

    public static List<List<TextToken>> splitOnSentences(EngText engText) {
        Set<String> learnGroups = new HashSet<>(engText.getListOfLearnGroups());
        Set<String> wordsToLearnRow = mapToSet(engText.getWords(), Word::getWordInText);
        map(
                filter(engText.getWords(), w -> learnGroups.contains(w.getGroup())),
                Word::getWordInText
        );
        Set<String> wordsFromCurrentGroupRow;
        if (learnGroups.contains(ALL_GROUPS)) {
            wordsFromCurrentGroupRow = wordsToLearnRow;
        } else {
            wordsFromCurrentGroupRow = mapToSet(
                    filter(engText.getWords(), w -> learnGroups.contains(w.getGroup())),
                    Word::getWordInText
            );
        }
        Set<String> ignoreListRow = new HashSet<>(Arrays.asList(engText.getIgnoreList().split("[\r\n]+")));
        Map<String, String> wordToGroupMap = new HashMap<>();
        engText.getWords().stream()
                .filter(word -> StringUtils.isNoneBlank(word.getWordInText()))
                .forEach(word ->
                        wordToGroupMap.put(
                                word.getWordInText(),
                                word.getGroup() == null ? "" : StringUtils.trim(word.getGroup())
                        )
                );

        return splitOnSentences(
                engText.getText(),
                wordsToLearnRow,
                wordsFromCurrentGroupRow,
                ignoreListRow,
                learnGroups,
                wordToGroupMap
        );
    }

    protected static List<List<TextToken>> splitOnSentences(String text,
                                                         Set<String> wordsToLearnRow,
                                                         Set<String> wordsFromSelectedGroupsRow,
                                                         Set<String> ignoreListRow,
                                                         Set<String> selectedGroupsRow,
                                                            Map<String, String> wordToGroupMap) {
        Set<String> wordsToLearn = getNonEmpty(wordsToLearnRow);
        Set<String> wordsFromSelectedGroups = getNonEmpty(wordsFromSelectedGroupsRow);
        Set<String> ignoreList = getNonEmpty(ignoreListRow);
        Set<String> selectedGroups = getNonEmpty(selectedGroupsRow);
        List<Object> tokensRow = extractUnsplittable(text);
        tokensRow = extractPredefinedParts(tokensRow, wordsToLearn);
        tokensRow = extractPredefinedParts(tokensRow, ignoreList);
        List<TextToken> tokens = tokenize(tokensRow);
        tokens = splitByLongestSequence(tokens, R_N);
        tokens = splitByLongestSequence(tokens, SENTENCE_ENDS);

        List<List<TextToken>> res = new LinkedList<>();
        List<TextToken> sentence = new LinkedList<>();
        for (TextToken token : tokens) {
            enhanceWithAttributes(
                    token,
                    ignoreList,
                    wordsToLearn,
                    wordsFromSelectedGroups,
                    selectedGroups,
                    wordToGroupMap
            );
            sentence.add(token);
            if (isEndOfSentence(token)) {
                res.add(sentence);
                sentence = new LinkedList<>();
            }
        }
        if (!sentence.isEmpty()) {
            res.add(sentence);
        }
        return res;
    }

    private static boolean isEndOfSentence(TextToken token) {
        return isSplittableBy(token, SENTENCE_ENDS);
    }

    private static Set<String> getNonEmpty(Set<String> strList) {
        return OutlineUtils.filter(strList, StringUtils::isNoneEmpty);
    }

    private static List<TextToken> splitByLongestSequence(List<TextToken> tokens, List<String> substrings) {
        List<TextToken> res = new LinkedList<>();
        for (TextToken token : tokens) {
            String val = token.getValue();
            if (isSplittableBy(token, substrings)) {
                int s = 0;
                while (s < val.length() && !substrings.contains(val.substring(s,s+1))) {
                    s++;
                }
                int e = s+1;
                while (e < val.length() && substrings.contains(val.substring(e,e+1))) {
                    e++;
                }
                if (s > 0) {
                    res.add(TextToken.builder().value(val.substring(0,s)).build());
                }
                res.add(TextToken.builder().value(val.substring(s,e)).build());
                if (e < val.length()) {
                    res.addAll(splitByLongestSequence(listF(TextToken.builder().value(val.substring(e)).build()), substrings));
                }
            } else {
                res.add(token);
            }
        }
        return res;
    }

    private static boolean containsOneOf(String str, List<String> substrings) {
        for (String sentenceEnd : substrings) {
            if (str.contains(sentenceEnd)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSplittableBy(TextToken token, List<String> substrings) {
        return !token.isUnsplittable() && containsOneOf(token.getValue(), substrings);
    }

    private static void enhanceWithAttributes(TextToken token,
                                              Set<String> ignoreList,
                                              Set<String> wordsToLearn,
                                              Set<String> wordsFromSelectedGroups,
                                              Set<String> selectedGroups,
                                              Map<String, String> wordToGroupMap) {
        String val = token.getValue();
        if (wordsToLearn.contains(val)) {
            token.setWord(true);
            token.setWordToLearn(true);
            if (wordsFromSelectedGroups.contains(val)) {
                token.setSelectedGroup(true);
            }
        } else if (!(ignoreList.contains(val) || HIDABLE_PATTERN.matcher(val).matches())) {
            token.setWord(true);
        }

        if (containsOneOf(val, R_N)) {
            token.setMeta(true);
        }

        token.setHiddable(
                token.isWord() && (token.isSelectedGroup() || selectedGroups.contains(ALL_WORDS))
        );

        if (token.isWordToLearn() && StringUtils.isEmpty(wordToGroupMap.get(val))) {
            token.setDoesntHaveGroup(true);
        }

        token.setGroup(wordToGroupMap.get(val));
    }

    private static List<Object> extractPredefinedParts(List<Object> res, Set<String> predefinedParts) {
        for (String wordToLearn : predefinedParts) {
            res = extractPredefinedPart(res, wordToLearn);
        }
        return res;
    }

    private static List<Object> extractUnsplittable(String text) {
        List<Object> res = new LinkedList<>();
        String tail = text;
        int idxS = tail.indexOf("[[");
        int idxE = idxS < 0 ? -1 : tail.indexOf("]]", idxS+2);
        while (idxE >= 2) {
            if (idxS > 0) {
                res.add(tail.substring(0, idxS));
            }
            res.add(TextToken.builder().value("[[").meta(true).build());
            res.add(TextToken.builder().value(tail.substring(idxS+2, idxE)).unsplittable(true).build());
            res.add(TextToken.builder().value("]]").meta(true).build());
            tail = tail.substring(idxE + 2);
            idxS = tail.indexOf("[[");
            idxE = idxS < 0 ? -1 : tail.indexOf("]]", idxS+2);
        }
        if (!tail.isEmpty()) {
            res.add(tail);
        }
        return res;
    }

    private static List<Object> extractPredefinedPart(List<Object> text, String wordToLearn) {
        List<Object> res = new LinkedList<>();
        for (Object obj : text) {
            if (obj instanceof TextToken) {
                res.add(obj);
            } else {
                String tail = (String) obj;
                int idx = tail.indexOf(wordToLearn);
                while (idx >= 0) {
                    res.add(tail.substring(0, idx));
                    res.add(TextToken.builder().value(wordToLearn).build());
                    tail = tail.substring(idx + wordToLearn.length());
                    idx = tail.indexOf(wordToLearn);
                }
                if (!tail.isEmpty()) {
                    res.add(tail);
                }
            }
        }
        return res;
    }



    private static List<TextToken> tokenize(List<Object> text) {
        List<TextToken> res = new LinkedList<>();
        for (Object obj : text) {
            if (obj instanceof TextToken) {
                res.add((TextToken) obj);
            } else {
                String str = (String) obj;
                for (String part : str.split(SENTENCE_PARTS_DELIMITER)) {
                    res.add(TextToken.builder().value(part).build());
                }
            }
        }
        return res;
    }
}
