package org.igye.outline.htmlforms;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EditTopicForm extends EditNodeForm {
    private String name;
    private UUID iconId;
    private boolean sol;
    private List<ContentForForm> content;
}
