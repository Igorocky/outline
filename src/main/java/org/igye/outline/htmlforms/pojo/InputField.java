package org.igye.outline.htmlforms.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InputField<T> {
    private InputType type;
    private String name;
    private T value;
    private Object valueObj;
    private String displayName;
}
