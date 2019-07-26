package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.map;

public class DtoConverter {

    public static final String ROOT_NODE = "ROOT_NODE";
    public static final String NODE = "NODE";
    public static final String TEXT_NODE = "TEXT";
    public static final String IMAGE_NODE = "IMAGE";

    public static NodeDto toDto(Node node, int depth, NodeDto request) {
        if (!Objects.equals(node.getId(), request.getId())) {
            throw new IllegalArgumentException("!Objects.equals(node.getId(), request.getId())");
        }
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setParentId(node.getParentNode() == null ? null : Optional.of(node.getParentNode().getId()));
        if (node.getId() == null) {
            nodeDto.setObjectClass(Optional.of(ROOT_NODE));
        }
        nodeDto.setName(Optional.ofNullable(node.getName()));
        if (depth > 0 || (request != null && request.getChildNodes().isPresent())) {
            if (!CollectionUtils.isEmpty(node.getChildNodes())) {
                nodeDto.setChildNodes(Optional.of(
                        map(node.getChildNodes(), n -> toDto(n,depth-1, getChildById(request, n.getId())))
                ));
            } else {
                nodeDto.setChildNodes(Optional.of(Collections.emptyList()));
            }
            nodeDto.getChildNodes().map(nodes -> {
                Collections.sort(nodes, Comparator.comparing(NodeDto::getOrd));
                return nodes;
            });
        }
        nodeDto.setIcon(node.getIcon() == null ? null : Optional.of(node.getIcon().getId()));
        nodeDto.setOrd(node.getOrd());

        if (node instanceof Text) {
            // TODO: 22.07.2019 tc: for texts objectClass == "..."
            nodeDto.setObjectClass(Optional.of(TEXT_NODE));
            nodeDto.setText(Optional.ofNullable(((Text) node).getText()));
        } else if (node instanceof ImageRef) {
            // TODO: 22.07.2019 tc: for images objectClass == "..."
            nodeDto.setObjectClass(Optional.of(IMAGE_NODE));
            nodeDto.setImgId(((ImageRef) node).getImage() == null ? null : Optional.of(((ImageRef) node).getImage().getId()));
        } else {
            // TODO: 22.07.2019 tc: for nodes objectClass == "..."
            nodeDto.setObjectClass(Optional.of(NODE));
        }

        return nodeDto;
    }

    private static NodeDto getChildById(NodeDto parent, UUID id) {
        if (parent == null) {
            return null;
        }
        return parent.getChildNodes().get().stream().filter(c -> c.getId().equals(id)).findFirst().get();
    }
}
