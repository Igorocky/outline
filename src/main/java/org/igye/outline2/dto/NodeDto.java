package org.igye.outline2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeDto {
    private UUID id;
    private UUID parentId;
    private String objectClass;
    private String name;
    private List<NodeDto> childNodes;
    private UUID icon;
    private int ord;
}
