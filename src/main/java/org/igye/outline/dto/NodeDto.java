package org.igye.outline.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeDto {
    private UUID id;
    private NodeClass clazz;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdWhen;

    private Map<TagId, List<TagValueDto>> tags = new HashMap<>();

    private UUID parentId;
    private List<NodeDto> childNodes;
}
