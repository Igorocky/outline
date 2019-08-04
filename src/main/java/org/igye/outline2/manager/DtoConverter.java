package org.igye.outline2.manager;

import org.igye.outline2.dto.ImageDto;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.PathElem;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.nullSafeGetterWithDefault;

public class DtoConverter {

    public static final String ROOT_NODE = "ROOT_NODE";
    public static final String NODE = "NODE";
    public static final String TEXT_NODE = "TEXT";
    public static final String IMAGE_NODE = "IMAGE";

    public static NodeDto toDto(Node node, int depth) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setParentId(node.getParentNode() == null ? null : Optional.of(node.getParentNode().getId()));
        nodeDto.setName(node.getName() == null ? null : Optional.of(node.getName()));
        if (depth > 0) {
            if (!CollectionUtils.isEmpty(node.getChildNodes())) {
                nodeDto.setChildNodes(Optional.of(
                        map(node.getChildNodes(), n -> toDto(n,depth-1))
                ));
            } else {
                nodeDto.setChildNodes(Optional.of(Collections.emptyList()));
            }
        }
        nodeDto.setIcon(node.getIcon() == null ? null : Optional.of(node.getIcon().getId()));

        if (node instanceof Text) {
            // TODO: 22.07.2019 tc: for texts objectClass == "..."
            Text textNode = (Text) node;
            nodeDto.setObjectClass(Optional.of(TEXT_NODE));
            nodeDto.setText(textNode.getText() == null ? null : Optional.of(textNode.getText()));
        } else if (node instanceof ImageRef) {
            // TODO: 22.07.2019 tc: for images objectClass == "..."
            nodeDto.setObjectClass(Optional.of(IMAGE_NODE));
            ImageRef imageRef = (ImageRef) node;
            nodeDto.setImgId(imageRef.getImage() == null ? null : Optional.of(imageRef.getImage().getId()));
        } else if (node.isTopNode()) {
            // TODO: 22.07.2019 tc: for root node objectClass == "..."
            nodeDto.setObjectClass(Optional.of(ROOT_NODE));
        } else {
            // TODO: 22.07.2019 tc: for nodes objectClass == "..."
            nodeDto.setObjectClass(Optional.of(NODE));
        }

        if (node.isTopNode()) {
            if (nodeDto.getChildNodes() != null && nodeDto.getChildNodes().isPresent()) {
                Collections.sort(
                        nodeDto.getChildNodes().get(),
                        Comparator.comparing(n ->
                                nullSafeGetterWithDefault(n.getName(), opt -> opt.orElse(""), "")
                        )
                );
            }
        }

        return nodeDto;
    }

    public static PathElem toPathElem(Node node) {
        return PathElem.builder().id(node.getId()).name(node.getName()).build();
    }

    public static ImageDto toDto(Image image) {
        ImageDto imageDto = new ImageDto();
        imageDto.setId(image.getId());
        return imageDto;
    }
}
