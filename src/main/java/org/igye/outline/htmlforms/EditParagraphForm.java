package org.igye.outline.htmlforms;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class EditParagraphForm {
    private UUID parentId;
    private UUID id;
    private String name;

    public UUID getIdToRedirectTo() {
        if (parentId != null) {
            return parentId;
        } else {
            return id;
        }
    }
}
