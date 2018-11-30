package org.igye.outline.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class TextToken {
    private String value;
    private String userInput;
    private Boolean correct;

    private boolean word;
    private boolean wordToLearn;
    private boolean meta;
    private boolean hidden;
}
