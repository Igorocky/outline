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
    private UUID iconId;
    private boolean eol;

    public UUID getIdToRedirectToIfCancelled() {
        if (id != null) {
            return id;
        } else {
            return parentId;
        }
    }
}
