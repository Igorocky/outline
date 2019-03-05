package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class NodesToLearnDto {
    private List<NodeDto> path;
    private List<NodeDto> nodesToLearn;
}
