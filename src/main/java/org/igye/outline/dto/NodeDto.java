package org.igye.outline.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class NodeDto {
    private UUID id;
    private String objectClass;
    private String name;
    private List<NodeDto> childNodes;
    private UUID icon;
    private UUID imgId;
    private String text;
}
