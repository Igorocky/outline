package org.igye.outline.htmlforms.pojo;

import lombok.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Form {
    private List<InputField<?>> fields;
    private String action;
    private String submitButtonText;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Predicate<InputField<?>> isHidden = f -> f.getType().equals(InputType.HIDDEN);
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Predicate<InputField<?>> isVisible = isHidden.negate();

    public List<InputField<?>> getAllHiddenFields() {
        return fields.stream().filter(isHidden).collect(Collectors.toList());
    }

    public List<InputField<?>> getAllVisibleFields() {
        return fields.stream().filter(isVisible).collect(Collectors.toList());
    }
}
