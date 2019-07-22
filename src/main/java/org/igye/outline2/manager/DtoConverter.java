package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import static org.igye.outline2.OutlineUtils.map;

public class DtoConverter {

    public static final String ROOT_NODE = "ROOT_NODE";

    public static NodeDto toDto(Node node, int depth) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setParentId(node.getParentNode() == null ? null : Optional.of(node.getParentNode().getId()));
        if (node.getId() != null) {
            nodeDto.setObjectClass(Optional.of(node.getClass().getSimpleName()));
        } else {
            nodeDto.setObjectClass(Optional.of(ROOT_NODE));
        }
        nodeDto.setName(Optional.ofNullable(node.getName()));
        if (depth > 0) {
            if (!CollectionUtils.isEmpty(node.getChildNodes())) {
                nodeDto.setChildNodes(Optional.of(map(node.getChildNodes(), n -> toDto(n,depth-1))));
            } else {
                nodeDto.setChildNodes(Optional.of(Collections.emptyList()));
            }
            nodeDto.getChildNodes().map(nodes -> {
                Collections.sort(nodes, Comparator.comparing(NodeDto::getOrd));
                return nodes;
            });
        }
        nodeDto.setIcon(Optional.of(node.getIconId()));
        nodeDto.setOrd(node.getOrd());

        if (node instanceof Text) {
            // TODO: 22.07.2019 tc: for texts objectClass == "..."
            nodeDto.setText(Optional.ofNullable(((Text) node).getText()));
        } else if (node instanceof Image) {
            // TODO: 22.07.2019 tc: for images objectClass == "..."
            nodeDto.setImgId(Optional.ofNullable(((Image) node).getImgId()));
        }

        return nodeDto;
    }
}
