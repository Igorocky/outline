package org.igye.outline2.chess.manager;

import org.igye.outline2.OutlineUtils;
import org.igye.outline2.chess.dto.ChessPuzzleCommentDto;
import org.igye.outline2.chess.dto.ChessPuzzleDto;
import org.igye.outline2.controllers.ControllerComponentTestBase;
import org.igye.outline2.controllers.TestClock;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.TagIds;
import org.igye.outline2.report.ReportManager;
import org.igye.outline2.report.ResultSetDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.mapOf;
import static org.igye.outline2.controllers.ComponentTestConfig.FIXED_DATE_TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChessPuzzleManagerComponentTest extends ControllerComponentTestBase {
    @Autowired
    private ChessPuzzleManager chessPuzzleManager;
    @Autowired
    private ReportManager reportManager;
    @Autowired
    private TestClock testClock;

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
        testClock.setFixedTime(FIXED_DATE_TIME);
        UUID puzzleId = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);

        //when
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId, false, "3d");

        //then
        ChessPuzzleDto puzzleDto = getPuzzleDto(puzzleId);
        assertEquals("false", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_PASSED));
        assertEquals("3d", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY));
        assertEquals(
                FIXED_DATE_TIME.toInstant().plus(3, ChronoUnit.DAYS),
                Instant.parse(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION))
        );

        //when
        testClock.setFixedTime(FIXED_DATE_TIME.plusSeconds(10));
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId, true, "12M");

        //then
        puzzleDto = getPuzzleDto(puzzleId);
        assertEquals("true", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_PASSED));
        assertEquals("12M", puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY));
        assertEquals(
                FIXED_DATE_TIME.toInstant().plusSeconds(10).plus(12*30, ChronoUnit.DAYS),
                Instant.parse(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION))
        );
        ResultSetDto resultSetDto = reportManager.rpcRunReport(
                "puzzle-history", Collections.singletonMap("puzzleId", puzzleId)
        );
        assertEquals(2, resultSetDto.getData().size());
        assertEquals("true", resultSetDto.getData().get(0).get("VALUE"));
        assertEquals("false", resultSetDto.getData().get(1).get("VALUE"));

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