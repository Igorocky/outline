package org.igye.outline.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TextToken {
    private String value;
    private String userInput;
    private Boolean correct;

    private boolean word;
    private boolean wordToLearn;
    private boolean hidden;
}
