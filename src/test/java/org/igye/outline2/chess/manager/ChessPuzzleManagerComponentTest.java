package org.igye.outline2.chess.manager;

import org.igye.outline2.common.OutlineUtils;
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

import static org.igye.outline2.common.OutlineUtils.MILLIS_IN_DAY;
import static org.igye.outline2.common.OutlineUtils.mapOf;
import static org.igye.outline2.controllers.ComponentTestConfig.FIXED_DATE_TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
        UUID puzzleId = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);

        //when1
        final Instant instant1 = FIXED_DATE_TIME.toInstant();
        testClock.setFixedTime(instant1);
        final boolean passed1 = false;
        final String activationDelay1 = "3d";
        final Instant expectedActivationInstant1 = instant1.plus(3, ChronoUnit.DAYS);
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId, passed1, activationDelay1);

        //then1
        ChessPuzzleDto puzzleDto = getPuzzleDto(puzzleId);
        assertEquals(Boolean.toString(passed1), puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_PASSED));
        assertEquals(activationDelay1, puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY));
        assertEquals(3*MILLIS_IN_DAY, Long.parseLong(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY_MS)));
        assertEquals(
                expectedActivationInstant1,
                Instant.parse(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION))
        );
        ResultSetDto resultSetDto = reportManager.rpcRunReport(
                "puzzle-history", Collections.singletonMap("puzzleId", puzzleId)
        );
        assertEquals(1, resultSetDto.getData().size());
        assertEquals(instant1.getEpochSecond()*1000, resultSetDto.getData().get(0).get("CREATED_WHEN"));
        assertEquals(Boolean.toString(passed1), resultSetDto.getData().get(0).get("PASSED"));

        //when2
        final Instant instant2 = instant1.plusSeconds(10);
        testClock.setFixedTime(instant2);
        final boolean passed2 = true;
        final String activationDelay2 = "12M";
        final Instant expectedActivationInstant2 = instant2.plus(12*30, ChronoUnit.DAYS);
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId, passed2, activationDelay2);

        //then2
        puzzleDto = getPuzzleDto(puzzleId);
        assertEquals(Boolean.toString(passed2), puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_PASSED));
        assertEquals(activationDelay2, puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY));
        assertEquals(12*30*MILLIS_IN_DAY, Long.parseLong(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY_MS)));
        assertEquals(
                expectedActivationInstant2,
                Instant.parse(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION))
        );
        resultSetDto = reportManager.rpcRunReport(
                "puzzle-history", Collections.singletonMap("puzzleId", puzzleId)
        );
        assertEquals(2, resultSetDto.getData().size());
        assertEquals(instant2.getEpochSecond()*1000, resultSetDto.getData().get(0).get("CREATED_WHEN"));
        assertEquals(Boolean.toString(passed2), resultSetDto.getData().get(0).get("PASSED"));
        assertEquals(instant1.getEpochSecond()*1000, resultSetDto.getData().get(1).get("CREATED_WHEN"));
        assertEquals(Boolean.toString(passed1), resultSetDto.getData().get(1).get("PASSED"));

        //when3
        final Instant instant3 = instant2.plusSeconds(20);
        testClock.setFixedTime(instant3);
        final boolean passed3 = true;
        final String activationDelay3 = "";
        final Instant expectedActivationInstant3 = null;
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId, passed3, activationDelay3);

        //then3
        puzzleDto = getPuzzleDto(puzzleId);
        assertEquals(Boolean.toString(passed3), puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_PASSED));
        assertEquals(activationDelay3, puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY));
        assertNull(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_DELAY_MS));
        assertNull(puzzleDto.getTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION));
        resultSetDto = reportManager.rpcRunReport(
                "puzzle-history", Collections.singletonMap("puzzleId", puzzleId)
        );
        assertEquals(3, resultSetDto.getData().size());
        assertEquals(instant3.getEpochSecond()*1000, resultSetDto.getData().get(0).get("CREATED_WHEN"));
        assertEquals(Boolean.toString(passed3), resultSetDto.getData().get(0).get("PASSED"));
        assertEquals(instant2.getEpochSecond()*1000, resultSetDto.getData().get(1).get("CREATED_WHEN"));
        assertEquals(Boolean.toString(passed2), resultSetDto.getData().get(1).get("PASSED"));
        assertEquals(instant1.getEpochSecond()*1000, resultSetDto.getData().get(2).get("CREATED_WHEN"));
        assertEquals(Boolean.toString(passed1), resultSetDto.getData().get(2).get("PASSED"));

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