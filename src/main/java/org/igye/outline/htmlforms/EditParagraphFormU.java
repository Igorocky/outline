package org.igye.outline.htmlforms;

import org.igye.outline.htmlforms.pojo.Form;
import org.igye.outline.htmlforms.pojo.InputField;
import org.igye.outline.htmlforms.pojo.InputType;

import java.util.Arrays;
import java.util.UUID;

public class EditParagraphFormU extends Form {
    public final InputField<UUID> PARENT_ID = InputField.<UUID>builder()
            .type(InputType.HIDDEN)
            .name("parentId")
            .build();
    public final InputField<UUID> ID = InputField.<UUID>builder()
            .type(InputType.HIDDEN)
            .name("id")
            .build();
    public final InputField<String> NAME = InputField.<String>builder()
            .type(InputType.TEXT)
            .name("name")
            .build();

    public EditParagraphFormU() {
        setFields(Arrays.asList(PARENT_ID, ID, NAME));
    }
}
