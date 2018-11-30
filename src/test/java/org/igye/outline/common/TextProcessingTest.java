package org.igye.outline.common;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.igye.outline.common.OutlineUtils.listF;
import static org.junit.Assert.assertEquals;

public class TextProcessingTest {
    @Test
    public void splitOnSentences_should_work_correctly() throws IOException {
        //given
        String text = IOUtils.resourceToString("/text-parsing/text-to-parse-1.txt", StandardCharsets.UTF_8);

        //when
        List<List<TextToken>> res = TextProcessing.splitOnSentences(
                text,
                listF("word3", "phrase to learn", ""),
                listF("word3", ""),
                listF("ignored", "")
        );

        //then
        assertEquals(3, res.size());
        int i = 0;
        assertEquals("!", res.get(i).get(res.get(i++).size()-1).getValue());
        assertEquals("!…", res.get(i).get(res.get(i++).size()-1).getValue());
        assertEquals(".", res.get(i).get(res.get(i++).size()-1).getValue());

        List<TextToken> flattened = new ArrayList<>();
        res.forEach(flattened::addAll);
        i = 0;
        assertEquals(TextToken.builder().value("Word1").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("1word2").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("word3").word(true).wordToLearn(true).selectedGroup(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("!").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("    ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("1Word").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(": ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("[[").meta(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("1word3").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("]]").meta(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("-").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("ignored").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("!…").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("\r\n").meta(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("\"").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("the").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("1word").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("\", ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("phrase to learn").word(true).wordToLearn(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("no").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(".").build(), flattened.get(i++));
        assertEquals(i, flattened.size());
    }
}