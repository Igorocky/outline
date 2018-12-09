package org.igye.outline.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.igye.outline.htmlforms.WordDto;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class ImportWords {
    @Test
    public void parseTable() throws IOException {
        List<WordDto> words = IOUtils.readLines(
                new FileInputStream(""), Charset.forName("UTF-8")
        ).stream()
                .map(line -> line.split("\t"))
                .map(parts ->
                        WordDto.builder()
                                .wordInText(parts[3].trim())
                                .word(parts[3].trim())
                                .transcription("[" + parts[4].trim() + "]")
                                .meaning(parts[5].trim())
                                .build()
                )
                .collect(Collectors.toList());
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(words));
    }
}
