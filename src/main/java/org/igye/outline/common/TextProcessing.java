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
    private static final String BORDER_SYMBOL = "[\\.?!\\u2026\\s\\r\\n,:;\"\\(\\)\\[\\]\\\\/\\*\\u201E\\u201D]";
    private static final String SENTENCE_PARTS_DELIMITER = "((?<=" + BORDER_SYMBOL + ")(?!" + BORDER_SYMBOL + "))|((?<!" + BORDER_SYMBOL + ")(?=" + BORDER_SYMBOL + "))";
    private static final Pattern HIDABLE_PATTERN = Pattern.compile("^[\\(\\)-.,\\s–\":\\[\\]\\\\/;!?\\u2014\\u2026\\u201E\\u201D]+$");
    private static final List<String> SENTENCE_ENDS = OutlineUtils.listF(".", "!", "?", "…");
    private static final List<String> R_N = OutlineUtils.listF("\r", "\n");

    public static List<List<TextToken>> splitOnSentences(String text, List<String> wordsToLearn, List<String> ignoreList) {
        List<TextToken> tokens = tokenize(extractWordsToLearn(extractUnsplittable(text), wordsToLearn));
        tokens = splitByLongestSequence(tokens, R_N);
        tokens = splitByLongestSequence(tokens, SENTENCE_ENDS);

        List<List<TextToken>> res = new LinkedList<>();
        List<TextToken> sentence = new LinkedList<>();
        for (TextToken token : tokens) {
            enhanceWithAttributes(token, ignoreList, wordsToLearn);
            sentence.add(token);
            if (containsOneOf(token.getValue(), SENTENCE_ENDS)) {
                res.add(sentence);
                sentence = new LinkedList<>();
            }
        }
        if (!sentence.isEmpty()) {
            res.add(sentence);
        }
        return res;
    }

    private static List<TextToken> splitByLongestSequence(List<TextToken> tokens, List<String> substrings) {
        List<TextToken> res = new LinkedList<>();
        for (TextToken token : tokens) {
            String val = token.getValue();
            if (containsOneOf(val, substrings)) {
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
                    res.add(TextToken.builder().value(val.substring(e)).build());
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

    private static void enhanceWithAttributes(TextToken token, List<String> ignoreList, List<String> wordsToLearn) {
        if (wordsToLearn.contains(token.getValue())) {
            token.setWordToLearn(true);
        }
        if (!(ignoreList.contains(token.getValue()) || HIDABLE_PATTERN.matcher(token.getValue()).matches())) {
            token.setWord(true);
        }
        if ("\r".equals(token.getValue()) || "\n".equals(token.getValue()) || "\r\n".equals(token.getValue())) {
            token.setMeta(true);
        }
    }

    public static List<Object> extractWordsToLearn(List<Object> res, List<String> wordsToLearn) {
        for (String wordToLearn : wordsToLearn) {
            res = extractWordToLearn(res, wordToLearn);
        }
        return res;
    }

    public static List<Object> extractUnsplittable(String text) {
        List<Object> res = new LinkedList<>();
        String tail = text;
        int idxS = tail.indexOf("[[");
        int idxE = idxS < 0 ? -1 : tail.indexOf("]]", idxS+2);
        while (idxE >= 0) {
            res.add(tail.substring(0, idxS));
            res.add(TextToken.builder().value("[[").meta(true).build());
            res.add(TextToken.builder().value(tail.substring(idxS+2, idxE)).build());
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

    private static List<Object> extractWordToLearn(List<Object> text, String wordToLearn) {
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
