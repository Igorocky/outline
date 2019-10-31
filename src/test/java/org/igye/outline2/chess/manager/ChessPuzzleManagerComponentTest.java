package org.igye.outline2.chess.manager;

import org.igye.outline2.OutlineUtils;
import org.igye.outline2.chess.dto.ChessPuzzleCommentDto;
import org.igye.outline2.chess.dto.ChessPuzzleDto;
import org.igye.outline2.controllers.ControllerComponentTestBase;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.TagIds;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.mapOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChessPuzzleManagerComponentTest extends ControllerComponentTestBase {
    @Autowired
    private ChessPuzzleManager chessPuzzleManager;

    @Test
    public void it_is_possible_to_create_and_update_puzzle_comments() throws ScriptException, NoSuchMethodException, IOException {
        //when
        UUID puzzleId = createNode(null, NodeClasses.CHESS_PUZZLE);

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

    @Test
    public void rpcSaveChessPuzzleAttempt_saves_and_updates_attempt_info() throws NoSuchMethodException, ScriptException, IOException {
        //given
        UUID puzzleId = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);

        //when
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId, false, "3d");

        //then
        ChessPuzzleDto puzzleDto = getPuzzleDto(puzzleId);
        assertEquals("false", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_PASSED));
        assertEquals("3d", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY));
        assertEqualsWithPrecision(
                Instant.now().plus(3, ChronoUnit.DAYS),
                Instant.parse(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION)),
                5
        );

        //when
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId, true, "12M");

        //then
        puzzleDto = getPuzzleDto(puzzleId);
        assertEquals("true", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_PASSED));
        assertEquals("12M", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY));
        assertEqualsWithPrecision(
                Instant.now().plus(12*30, ChronoUnit.DAYS),
                Instant.parse(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION)),
                5
        );
    }

    private void assertEqualsWithPrecision(Instant expected, Instant actual, long precisionSeconds) {
        assertTrue(Math.abs(Duration.between(expected, actual).getSeconds()) <= precisionSeconds);
    }

    private ChessPuzzleDto getPuzzleDto(UUID puzzleId) throws ScriptException, NoSuchMethodException, IOException {
        return objectMapper.readValue(
                invokeJsRpcFunction("getNode", OutlineUtils.mapOf("id", puzzleId)),
                ChessPuzzleDto.class
        );
    }

    private UUID createNode(UUID parentId, String clazz) throws ScriptException, NoSuchMethodException, IOException {
        return objectMapper.readValue(
                invokeJsRpcFunction("createNode", parentId, clazz),
                UUID.class
        );
    }


}