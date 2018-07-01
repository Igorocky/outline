package org.igye.outline.htmlforms;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EditSynopsisTopicForm implements HasIdToRedirectTo {
    private UUID parentId;
    private UUID id;
    private String name;
    private List<ContentForForm> content;

    @Override
    public UUID getIdToRedirectTo() {
        if (parentId != null) {
            return parentId;
        } else {
            return id;
        }
    }
}
