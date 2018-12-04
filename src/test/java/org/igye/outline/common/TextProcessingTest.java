package org.igye.outline.common;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.igye.outline.common.OutlineUtils.setF;
import static org.junit.Assert.assertEquals;

public class TextProcessingTest {
    @Test
    public void splitOnSentences_should_work_correctly() throws IOException {
        //given
        String text = IOUtils.resourceToString("/text-parsing/text-to-parse-1.txt", StandardCharsets.UTF_8);

        //when
        List<List<TextToken>> res = TextProcessing.splitOnSentences(
                text,
                setF("word3", "phrase to learn", ""),
                setF("word3", ""),
                setF("ignored", ""),
                setF("grp1"),
                ImmutableMap.of(
                        "word3", "",
                        "phrase to learn", "grp1"
                )
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
        assertEquals(TextToken.builder().value("word3").word(true).group("").wordToLearn(true).selectedGroup(true).hiddable(true).doesntHaveGroup(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("!").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("    ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("1Word").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(": ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("[[").meta(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("1word3").word(true).unsplittable(true).build(), flattened.get(i++));
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
        assertEquals(TextToken.builder().value("phrase to learn").word(true).group("grp1").wordToLearn(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(" ").build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("no").word(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value(".").build(), flattened.get(i++));
        assertEquals(i, flattened.size());
    }

    @Test
    public void splitOnSentences_should_work_correctly_2() throws IOException {
        //given
        String text = "[[site.com]]";

        //when
        List<List<TextToken>> res = TextProcessing.splitOnSentences(
                text,
                setF(),
                setF(),
                setF(),
                setF(),
                ImmutableMap.of()
        );

        //then
        assertEquals(1, res.size());
        int i = 0;
        List<TextToken> flattened = new ArrayList<>();
        res.forEach(flattened::addAll);

        assertEquals(TextToken.builder().value("[[").meta(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("site.com").word(true).unsplittable(true).build(), flattened.get(i++));
        assertEquals(TextToken.builder().value("]]").meta(true).build(), flattened.get(i++));
        assertEquals(i, flattened.size());
    }
}