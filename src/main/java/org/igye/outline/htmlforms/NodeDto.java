package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class NodeDto {
    private UUID id;
    private UUID iconId;
    private String title;
    private String url;
}
