package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline.common.TextToken;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngTextDto {
    private UUID textId;
    private String title;
    private String text;
    private List<List<TextToken>> sentences;
    private List<WordDto> wordsToLearn;
    private String ignoreList;
    private String learnGroup;
}
