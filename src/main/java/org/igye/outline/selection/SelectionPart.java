package org.igye.outline.selection;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SelectionPart {
    private SelectionType selectionType;
    private UUID selectedId;
}
