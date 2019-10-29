package org.igye.outline2.manager;

import org.igye.outline2.dto.ChessPuzzleDto;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.dto.TagDto;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.Tag;
import org.springframework.util.CollectionUtils;

import java.util.Collections;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;

public class DtoConverter {

    public static NodeDto toDto(Node node, int depth) {
        NodeDto nodeDto = createNodeDto(node.getClazz());
        nodeDto.setId(node.getId());
        nodeDto.setClazz(new OptVal<>(node.getClazz()));
        nodeDto.setCreatedWhen(node.getCreatedWhen());
        nodeDto.setParentId(nullSafeGetter(
                node.getParentNode(),
                parentNode -> new OptVal<>(parentNode.getId())
        ));
        nodeDto.setTags(map(node.getTags(), DtoConverter::toDto));


        if (depth > 0) {
            if (!CollectionUtils.isEmpty(node.getChildNodes())) {
                nodeDto.setChildNodes(map(node.getChildNodes(), n -> toDto(n,depth-1)));
            } else {
                nodeDto.setChildNodes(Collections.emptyList());
            }
        }

        return nodeDto;
    }

    public static TagDto toDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .node(tag.getNode().getId())
                .tagId(new OptVal<>(tag.getTagId()))
                .value(new OptVal<>(tag.getValue()))
                .build();
    }

    private static NodeDto createNodeDto(String clazz) {
        if (NodeClasses.CHESS_PUZZLE.equals(clazz)) {
            return new ChessPuzzleDto();
        } else {
            return new NodeDto();
        }
    }
}
