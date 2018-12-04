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
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class TextToken {
    private String value;
    private String group;

    private boolean unsplittable;
    private boolean word;
    private boolean wordToLearn;
    private boolean selectedGroup;
    private boolean doesntHaveGroup;
    private boolean meta;
    private boolean hiddable;

    private boolean hidden;
    private String userInput;
    private Boolean correct;
}
