package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessPuzzleCommentDto;
import org.igye.outline2.chess.dto.ChessPuzzleDto;
import org.igye.outline2.common.OutlineUtils;
import org.igye.outline2.controllers.ControllerComponentTestBase;
import org.igye.outline2.controllers.OutlineTestUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.igye.outline2.common.OutlineUtils.MILLIS_IN_DAY;
import static org.igye.outline2.common.OutlineUtils.mapOf;
import static org.igye.outline2.common.OutlineUtils.mapToSet;
import static org.igye.outline2.common.OutlineUtils.setOf;
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

    @Test public void it_is_possible_to_create_and_update_puzzle_comments() throws ScriptException, NoSuchMethodException, IOException {
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
    @Test public void rpcSaveChessPuzzleAttempt_saves_and_updates_attempt_info() throws NoSuchMethodException, ScriptException, IOException {
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
    @Test public void calculateDelaySeconds_correctlyCalculatesWithRandomFactor() {
        calculateDelaySeconds_correctlyCalculatesWithRandomFactor(
                "1hr4", 4, ChronoUnit.HOURS.getDuration().getSeconds(), 120
        );
        calculateDelaySeconds_correctlyCalculatesWithRandomFactor(
                "2dr", 3, ChronoUnit.DAYS.getDuration().getSeconds()*2,
                2*60*18
        );
    }
    @Test public void puzzlesToRepeat_report_doesnt_return_paused_puzzles() throws IOException {
        //given
        OutlineTestUtils.deleteAllNodes(nodeRepository);
        UUID puzzleId1 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId2 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId3 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId4 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId5 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        nodeManager.rpcSetSingleTagForNode(puzzleId2,TagIds.CHESS_PUZZLE_PAUSED,"true");
        nodeManager.rpcSetSingleTagForNode(puzzleId3,TagIds.CHESS_PUZZLE_PAUSED,"true");
        nodeManager.rpcSetSingleTagForNode(puzzleId4,TagIds.CHESS_PUZZLE_PAUSED,"false");
        nodeManager.rpcSetSingleTagForNode(puzzleId5,TagIds.CHESS_PUZZLE_PAUSED,"0");

        //when
        ResultSetDto resultSetDto = reportManager.rpcRunReport("puzzles-to-repeat", Collections.emptyMap());

        //then
        assertEquals(
                setOf(puzzleId1, puzzleId4, puzzleId5),
                mapToSet(resultSetDto.getData(), row -> row.get("ID"))
        );
    }
    @Test public void puzzlesToRepeat_report_puzzles_without_delay_are_shown_in_first_lines() throws IOException {
        //given
        OutlineTestUtils.deleteAllNodes(nodeRepository);
        UUID puzzleId1 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId2 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId3 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId4 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId5 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId6 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId1, false, "1d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId2, false, "");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId3, false, "1d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId4, false, "1d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId5, false, "");

        //when
        ResultSetDto resultSetDto = reportManager.rpcRunReport("puzzles-to-repeat", Collections.emptyMap());

        //then
        assertEquals(
                setOf(puzzleId2, puzzleId5, puzzleId6),
                setOf(
                        resultSetDto.getData().get(0).get("ID"),
                        resultSetDto.getData().get(1).get("ID"),
                        resultSetDto.getData().get(2).get("ID")
                )
        );
    }
    @Test public void puzzlesToRepeat_report_puzzles_are_sorted_by_delay_overdue_pct_desc() throws IOException {
        //given
        OutlineTestUtils.deleteAllNodes(nodeRepository);
        UUID puzzleId1 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId2 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId3 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId4 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId5 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);

        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId2, false, null);
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId4, false, "1d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId1, false, "2d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId3, false, "3d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId5, false, "4d");

        //when
        ResultSetDto resultSetDto = reportManager.rpcRunReport("puzzles-to-repeat", Collections.emptyMap());

        //then
        assertEquals(puzzleId2, resultSetDto.getData().get(0).get("ID"));
        assertEquals(puzzleId4, resultSetDto.getData().get(1).get("ID"));
        assertEquals(puzzleId1, resultSetDto.getData().get(2).get("ID"));
        assertEquals(puzzleId3, resultSetDto.getData().get(3).get("ID"));
        assertEquals(puzzleId5, resultSetDto.getData().get(4).get("ID"));
    }
    @Test public void puzzlesToRepeat_report_returns_row_numbers() throws IOException {
        //given
        OutlineTestUtils.deleteAllNodes(nodeRepository);
        UUID puzzleId1 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId2 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId3 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId4 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);
        UUID puzzleId5 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);

        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId2, false, null);
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId4, false, "1d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId1, false, "2d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId3, false, "3d");
        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId5, false, "4d");

        //when
        ResultSetDto resultSetDto = reportManager.rpcRunReport("puzzles-to-repeat", Collections.emptyMap());

        //then
        assertEquals(1L, resultSetDto.getData().get(0).get("RN"));
        assertEquals(2L, resultSetDto.getData().get(1).get("RN"));
        assertEquals(3L, resultSetDto.getData().get(2).get("RN"));
        assertEquals(4L, resultSetDto.getData().get(3).get("RN"));
        assertEquals(5L, resultSetDto.getData().get(4).get("RN"));
    }
    @Test public void puzzlesToRepeat_report_shows_remaining_time_for_puzzles_without_overdue() throws IOException {
        //given
        OutlineTestUtils.deleteAllNodes(nodeRepository);
        OutlineUtils.setClock(testClock);
        UUID puzzleId1 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);

        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId1, false, "2M");

        //when
        testClock.plus(25, ChronoUnit.DAYS);
        ResultSetDto resultSetDto = reportManager.rpcRunReport("puzzles-to-repeat", Collections.emptyMap());

        //then
        assertEquals("1M 5d", resultSetDto.getData().get(0).get("DELAY"));
    }
    @Test public void puzzlesToRepeat_report_shows_overdue_pct_for_puzzles_with_overdue() throws IOException {
        //given
        OutlineTestUtils.deleteAllNodes(nodeRepository);
        OutlineUtils.setClock(testClock);
        UUID puzzleId1 = nodeManager.rpcCreateNode(null, NodeClasses.CHESS_PUZZLE);

        chessPuzzleManager.rpcSaveChessPuzzleAttempt(puzzleId1, false, "3d");

        //when
        testClock.plus(5, ChronoUnit.DAYS);
        ResultSetDto resultSetDto = reportManager.rpcRunReport("puzzles-to-repeat", Collections.emptyMap());

        //then
        assertEquals("67%", resultSetDto.getData().get(0).get("DELAY"));
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

    private void calculateDelaySeconds_correctlyCalculatesWithRandomFactor(
            String delayStr, int randomFactorPct, long baseDurationSeconds, int bucketWidthSeconds) {
        //given
        double proc = randomFactorPct / 10.0;
        long left = (long) (baseDurationSeconds * (1.0 - proc));
        long right = (long) (baseDurationSeconds * (1.0 + proc));
        long range = right - left;
        long expectedNumOfBuckets = range/bucketWidthSeconds;
        Map<Integer,Integer> counts = new HashMap<>();
        final long expectedAvg = 500;
        final long numOfCalcs = expectedNumOfBuckets * expectedAvg;

        //when
        for (int i = 0; i < numOfCalcs; i++) {
            final Long actualDelay = chessPuzzleManager.calculateDelaySeconds(delayStr);
            long diff = actualDelay - left;
            diff = right == actualDelay ? diff - 1 : diff;
            int bucketNum = (int) (diff / bucketWidthSeconds);
            inc(counts, bucketNum);
        }

        //then
        assertEquals(expectedNumOfBuckets, counts.size());
        for (Map.Entry<Integer, Integer> countsEntry : counts.entrySet()) {
            final double deltaPct = Math.abs((expectedAvg - countsEntry.getValue()) / (expectedAvg * 1.0));
            assertTrue(
                    "bucketNum = " + countsEntry.getKey() + ", expectedAvg = " + expectedAvg
                            + ", actualCount = " + countsEntry.getValue() + ", deltaPct = " + deltaPct,
                    deltaPct < 0.2
            );
        }
    }

    private void inc(Map<Integer,Integer> counts, int key) {
        Integer val = counts.get(key);
        if (val == null) {
            val = 0;
        }
        counts.put(key, val+1);
    }
}