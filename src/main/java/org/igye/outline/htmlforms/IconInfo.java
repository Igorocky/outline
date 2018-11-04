package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IconInfo {
    private UUID iconId;
    private CellType cellType;
    private UUID nodeId;
    private Integer number;
}
