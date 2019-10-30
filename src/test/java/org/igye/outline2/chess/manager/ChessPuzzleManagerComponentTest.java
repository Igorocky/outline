package org.igye.outline2.chess.manager;

import org.igye.outline2.OutlineUtils;
import org.igye.outline2.chess.dto.ChessPuzzleCommentDto;
import org.igye.outline2.chess.dto.ChessPuzzleDto;
import org.igye.outline2.controllers.ControllerComponentTestBase;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.TagIds;
import org.junit.Test;

import javax.script.ScriptException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.mapOf;
import static org.igye.outline2.controllers.OutlineTestUtils.doNotSerialize;
import static org.junit.Assert.*;

public class ChessPuzzleManagerComponentTest extends ControllerComponentTestBase {
    @Test
    public void it_is_possible_to_create_and_update_puzzle_comments() throws ScriptException, NoSuchMethodException, IOException {
        //when
        UUID puzzleId = createNode(null, NodeClasses.CHESS_PUZZLE, ChessPuzzleDto.class).getId();

        //then
        ChessPuzzleDto chessPuzzleDto = getPuzzleDto(puzzleId);
        assertTrue(chessPuzzleDto.getComments().isEmpty());

        //when
        invokeJsRpcFunction("saveCommentForChessPuzzle", mapOf(
                        "puzzleId", puzzleId,
                        "text", "comment-1"
        ));

        //then
        chessPuzzleDto = getPuzzleDto(puzzleId);
        final List<ChessPuzzleCommentDto> comments = chessPuzzleDto.getComments();
        assertEquals(1, comments.size());
        assertEquals("comment-1", comments.get(0).getText());


    }

    private ChessPuzzleDto getPuzzleDto(UUID puzzleId) throws ScriptException, NoSuchMethodException, IOException {
        return objectMapper.readValue(
                invokeJsRpcFunction("getNode", OutlineUtils.mapOf("id", puzzleId)),
                ChessPuzzleDto.class
        );
    }

    private <T> T createNode(UUID parentId, String clazz, Class<T> expectedResponseType) throws ScriptException, NoSuchMethodException, IOException {
        return objectMapper.readValue(
                invokeJsRpcFunction("createNode", parentId, clazz),
                expectedResponseType
        );
    }


}