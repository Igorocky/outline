package org.igye.outline.htmlforms;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ReorderParagraphChildren {
    private UUID parentId;
    private List<UUID> paragraphs;
    private List<UUID> topics;
}
