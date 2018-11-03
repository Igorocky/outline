package org.igye.outline.selection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectionPart {
    private ObjectType objectType;
    private UUID selectedId;
}
