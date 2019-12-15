package org.igye.outline2.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.igye.outline2.chess.dto.ChessGameDto;
import org.igye.outline2.chess.dto.ChessPuzzleCommentDto;
import org.igye.outline2.chess.dto.ChessPuzzleDto;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionDto;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.dto.TagDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.Tag;
import org.igye.outline2.pm.TagIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.igye.outline2.common.OutlineUtils.map;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetter;

@Component
public class DtoConverter {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;

    public NodeDto toDto(Node node, int depth, Predicate<Tag> tagFilter) {
        NodeDto nodeDto = createNodeDto(node.getClazz());
        nodeDto.setId(node.getId());
        nodeDto.setClazz(new OptVal<>(node.getClazz()));
        nodeDto.setCreatedWhen(node.getCreatedWhen());
        nodeDto.setParentId(nullSafeGetter(
                node.getParentNode(),
                parentNode -> new OptVal<>(parentNode.getId())
        ));
        nodeDto.setTags(map(node.getTags(), this::toDto));
        nodeDto.setTags(node.getTags().stream().filter(tagFilter).map(this::toDto).collect(Collectors.toList()));


        if (depth > 0) {
            if (!CollectionUtils.isEmpty(node.getChildNodes())) {
                nodeDto.setChildNodes(map(node.getChildNodes(), n -> toDto(n,depth-1,tagFilter)));
            } else {
                nodeDto.setChildNodes(Collections.emptyList());
            }
        }
        try {
            enrich(nodeDto, node);
        } catch (IOException ex) {
            throw new OutlineException(ex);
        }

        return nodeDto;
    }

    public TagDto toDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .node(tag.getNode().getId())
                .tagId(new OptVal<>(tag.getTagId()))
                .value(new OptVal<>(tag.getValue()))
                .build();
    }

    public void enrich(ParsedPgnDto parsedPgnDto) {
        Map<String, PositionDto> fens = new HashMap();
        for (List<PositionDto> fullMove : parsedPgnDto.getPositions()) {
            for (PositionDto halfMove : fullMove) {
                halfMove.setPuzzleIds(new ArrayList<>());
                fens.put(halfMove.getFen(), halfMove);
            }
        }
        jdbcTemplate.query(
                "select fen.value fen, fen.NODE_ID puzzle_id\n" +
                        " from tag fen\n" +
                        " where fen.tag_id = 'chess_puzzle_fen'"
                        + " and fen.value in (:fens)",
                Collections.singletonMap("fens", fens.keySet()),
                rs -> {
                    fens.get(rs.getString("fen")).getPuzzleIds().add(UUID.fromString(
                            rs.getString("puzzle_id")
                    ));
                }
        );
    }

    private NodeDto createNodeDto(String clazz) {
        if (NodeClasses.CHESS_PUZZLE.equals(clazz)) {
            return new ChessPuzzleDto();
        } else if (NodeClasses.CHESS_GAME.equals(clazz)) {
            return new ChessGameDto();
        } else {
            return new NodeDto();
        }
    }

    private void enrich(NodeDto nodeDto, Node node) throws IOException {
        if (nodeDto instanceof ChessPuzzleDto) {
            enrich((ChessPuzzleDto) nodeDto, node);
        } else if (nodeDto instanceof ChessGameDto) {
            enrich((ChessGameDto) nodeDto, node);
        }
    }

    private void enrich(ChessPuzzleDto chessPuzzleDto, Node node) {
        for (Node childNode : node.getChildNodes()) {
            if (NodeClasses.CHESS_PUZZLE_COMMENT.equals(childNode.getClazz())) {
                chessPuzzleDto.getComments().add(ChessPuzzleCommentDto.builder()
                        .id(childNode.getId())
                        .text(childNode.getTagSingleValue(TagIds.CHESS_PUZZLE_COMMENT_TEXT))
                        .build());
            }
        }
    }

    private void enrich(ChessGameDto chessGameDto, Node node) throws IOException {
        if (chessGameDto.getTags() != null) {
            final String parsedPgnStr = chessGameDto.getTagSingleValue(TagIds.CHESS_GAME_PARSED_PGN);
            if (parsedPgnStr != null) {
                ParsedPgnDto parsedPgnDto = objectMapper.readValue(parsedPgnStr, ParsedPgnDto.class);
                chessGameDto.setParsedPgn(parsedPgnDto);
                chessGameDto.removeTags(TagIds.CHESS_GAME_PARSED_PGN);
                enrich(parsedPgnDto);
            }
        }
    }
}
