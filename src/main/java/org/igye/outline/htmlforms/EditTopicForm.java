package org.igye.outline.htmlforms;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EditTopicForm {
    private UUID parentId;
    private UUID id;
    private String name;
    private UUID iconId;
    private boolean eol;
    private List<ContentForForm> content;
}
