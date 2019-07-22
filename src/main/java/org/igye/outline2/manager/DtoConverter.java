package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;

import static org.igye.outline2.OutlineUtils.map;

public class DtoConverter {

    public static final String ROOT_NODE = "ROOT_NODE";

    public static NodeDto toDto(Node node, int depth) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setParentId(node.getParentNode() != null ? node.getParentNode().getId() : null);
        if (node.getId() != null) {
            nodeDto.setObjectClass(node.getClass().getSimpleName());
        } else {
            nodeDto.setObjectClass(ROOT_NODE);
        }
        nodeDto.setName(node.getName());
        if (depth > 0 && !CollectionUtils.isEmpty(node.getChildNodes())) {
            nodeDto.setChildNodes(map(node.getChildNodes(), n -> toDto(n,depth-1)));
            Collections.sort(nodeDto.getChildNodes(), Comparator.comparing(NodeDto::getOrd));
        }
        nodeDto.setIcon(node.getIcon() != null ? node.getIcon().getId() : null);
        nodeDto.setOrd(node.getOrd());

        if (node instanceof Text) {
            nodeDto.setText(((Text) node).getText());
        } else if (node instanceof Image) {
            nodeDto.setImgId(((Image) node).getImgId());
        }

        return nodeDto;
    }
}
