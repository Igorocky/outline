package org.igye.outline2.manager;

import org.igye.outline2.OutlineUtils;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Node;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;

public class DtoConverter {
    public static NodeDto toDto(Node node, int depth) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setParentId(node.getParentNode().getId());
        nodeDto.setObjectClass(node.getClass().getSimpleName());
        nodeDto.setName(node.getName());
        if (depth > 0 && !CollectionUtils.isEmpty(node.getChildNodes())) {
            nodeDto.setChildNodes(OutlineUtils.map(node.getChildNodes(), n -> toDto(n,depth-1)));
            Collections.sort(nodeDto.getChildNodes(), Comparator.comparing(NodeDto::getOrd));
        }
        nodeDto.setIcon(node.getIcon().getId());
        nodeDto.setOrd(node.getOrd());
        return nodeDto;
    }
}
