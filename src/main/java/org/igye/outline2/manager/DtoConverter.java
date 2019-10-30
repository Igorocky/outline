package org.igye.outline2.manager;

import org.igye.outline2.chess.dto.ChessPuzzleCommentDto;
import org.igye.outline2.chess.dto.ChessPuzzleDto;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.dto.TagDto;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.Tag;
import org.igye.outline2.pm.TagIds;
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
        enrich(nodeDto, node);

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

    private static void enrich(NodeDto nodeDto, Node node) {
        if (nodeDto instanceof ChessPuzzleDto) {
            enrich((ChessPuzzleDto) nodeDto, node);
        }
    }

    private static void enrich(ChessPuzzleDto chessPuzzleDto, Node node) {
        for (Node childNode : node.getChildNodes()) {
            if (NodeClasses.CHESS_PUZZLE_COMMENT.equals(childNode.getClazz())) {
                chessPuzzleDto.getComments().add(ChessPuzzleCommentDto.builder()
                        .id(childNode.getId())
                        .text(childNode.getTagSingleValue(TagIds.CHESS_PUZZLE_COMMENT_TEXT))
                        .build());
            }
        }
    }
}
