package org.igye.outline.htmlforms;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class EditNodeForm {
    protected UUID parentId;
    protected UUID id;

    public UUID getIdToRedirectToIfCancelled() {
        if (id != null) {
            return id;
        } else {
            return parentId;
        }
    }
}
