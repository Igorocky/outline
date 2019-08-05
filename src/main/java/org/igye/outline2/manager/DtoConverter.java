package org.igye.outline2.manager;

import org.igye.outline2.dto.ImageDto;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.TagDto;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Tag;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Optional;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;

public class DtoConverter {

    public static NodeDto toDto(Node node, int depth) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setCreatedWhen(node.getCreatedWhen());
        nodeDto.setParentId(nullSafeGetter(
                node.getParentNode(),
                parentNode -> parentNode.getId(),
                id -> Optional.of(id)
        ));
        nodeDto.setTags(Optional.of(map(node.getTags(), DtoConverter::toDto)));


        if (depth > 0) {
            if (!CollectionUtils.isEmpty(node.getChildNodes())) {
                nodeDto.setChildNodes(Optional.of(
                        map(node.getChildNodes(), n -> toDto(n,depth-1))
                ));
            } else {
                nodeDto.setChildNodes(Optional.of(Collections.emptyList()));
            }
        }

        return nodeDto;
    }

    public static TagDto toDto(Tag tag) {
        TagDto tagDto = new TagDto();
        tagDto.setTagId(tag.getTagId());
        tagDto.setValue(tag.getValue());
        return tagDto;
    }

    public static ImageDto toDto(Image image) {
        ImageDto imageDto = new ImageDto();
        imageDto.setId(image.getId());
        return imageDto;
    }
}
