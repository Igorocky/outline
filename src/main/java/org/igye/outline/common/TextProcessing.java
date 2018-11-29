package org.igye.outline.common;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class TextProcessing {
    /*
       '—' - 2014
       '…' - 2026
       '„' - 201E
       '”' - 201D
      */
    private static final String BORDER_SYMBOL = "[\\s\\r\\n,:;\\.\"\\(\\)\\[\\]\\\\/!?\\*\\u2026\\u201E\\u201D]";
    private static final String SENTENCE_PARTS_DELIMITER = "((?<=" + BORDER_SYMBOL + ")(?!" + BORDER_SYMBOL + "))|((?<!" + BORDER_SYMBOL + ")(?=" + BORDER_SYMBOL + "))";
    private static final Pattern HIDABLE_PATTERN = Pattern.compile("^[\\(\\)-.,\\s–\":\\[\\]\\\\/;!?\\u2014\\u2026\\u201E\\u201D]+$");

    public static List<List<TextToken>> splitOnSentences(String text, List<String> unsplitable, List<String> ignoreList, List<String> wordsToLearn) {
        List<TextToken> tokens = tokenize(text, unsplitable);
        List<List<TextToken>> res = new LinkedList<>();
        List<TextToken> sentence = new LinkedList<>();
        for (TextToken token : tokens) {
            enhanceWithAttributes(token, ignoreList, wordsToLearn);
            sentence.add(token);
            if (isEndOfSentence(token.getValue())) {
                res.add(sentence);
                sentence = new LinkedList<>();
            }
        }
        if (!sentence.isEmpty()) {
            res.add(sentence);
        }
        return res;
    }

    private static boolean isEndOfSentence(String str) {
        String trimmed = str.trim();
        return trimmed.startsWith(".")
                || trimmed.startsWith("?")
                || trimmed.startsWith("!")
                || trimmed.startsWith("…");
    }

    private static void enhanceWithAttributes(TextToken token, List<String> ignoreList, List<String> wordsToLearn) {
        if (wordsToLearn.contains(token.getValue())) {
            token.setWordToLearn(true);
        }
        if (!(ignoreList.contains(token.getValue()) || HIDABLE_PATTERN.matcher(token.getValue()).matches())) {
            token.setWord(true);
        }
    }

    public static List<TextToken> tokenize(String text, List<String> unsplitable) {
        List<Object> res = new LinkedList<>();
        res.add(text);
        for (String unsplitablePart : unsplitable) {
            res = tokenize(res, unsplitablePart);
        }
        return tokenize(res);
    }

    private static List<Object> tokenize(List<Object> text, String unsplitable) {
        List<Object> res = new LinkedList<>();
        for (Object obj : text) {
            if (obj instanceof TextToken) {
                res.add(obj);
            } else {
                String str = (String) obj;
                int idx = str.indexOf(unsplitable);
                while (idx >= 0) {
                    res.add(str.substring(0, idx));
                    res.add(TextToken.builder().value(unsplitable).build());
                    str = str.substring(idx + unsplitable.length());
                    idx = str.indexOf(unsplitable);
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
