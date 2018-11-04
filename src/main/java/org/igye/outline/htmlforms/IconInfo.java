package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.igye.outline.selection.ObjectType;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IconInfo {
    private UUID iconId;
    private ObjectType objectType;
    private UUID nodeId;
}
