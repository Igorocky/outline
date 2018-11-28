package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordDto {
    private UUID id;
    private String group;
    private String wordInText;
    private String word;
    private String transcription;
    private String meaning;
}
