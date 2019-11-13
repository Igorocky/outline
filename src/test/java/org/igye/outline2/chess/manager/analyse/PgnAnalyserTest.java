package org.igye.outline2.chess.manager.analyse;

import org.igye.outline2.chess.dto.MoveAnalysisDto;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionAnalysisDto;
import org.igye.outline2.chess.dto.PositionDto;
import org.igye.outline2.common.OutlineUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.igye.outline2.chess.manager.analyse.FenAnalyser.compareMoves;
import static org.igye.outline2.chess.manager.analyse.PgnAnalyser.calcDelta;
import static org.igye.outline2.common.OutlineUtils.toJson;
import static org.igye.outline2.controllers.OutlineTestUtils.STOCKFISH_CMD;
import static org.igye.outline2.controllers.OutlineTestUtils.move;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PgnAnalyserTest {

    @Test
    public void analysePgn_sets_estimations() throws IOException {
        //given
        String pgnStr = OutlineUtils.readStringFromClasspath("/test-data/pgn/test-analysis.pgn");

        //when
        ParsedPgnDto parsedPgnDto = PgnAnalyser.analysePgn(
                STOCKFISH_CMD, pgnStr, 5, null, 4, null
        );

        //then
        int halfMoveCnt = 0;
        for (List<PositionDto> fullMove : parsedPgnDto.getPositions()) {
            boolean isWhite = false;
            for (PositionDto halfMove : fullMove) {
                isWhite = !isWhite;
                halfMoveCnt++;
                List<MoveAnalysisDto> moves = halfMove.getAnalysis().getPossibleMoves();
                for (int i = 0; i < moves.size() - 1; i++) {
                    int cmpSgn = compareMoves(moves.get(i), moves.get(i + 1));
                    assertTrue(
                            "error for i = " + i + ", cmpSgn = " + cmpSgn + ":\n" + toJson(halfMove),
                            cmpSgn == (isWhite?1:-1) || cmpSgn == 0
                    );
                }
            }
        }
        assertEquals(38, halfMoveCnt);
    }


    @Test
    public void calcDelta_calculates_delta_correctly() {
        assertEquals(
                25,
                calcDelta(
                        analysisRes(move(null, 23)),
                        analysisRes(move(null, 48)),
                        true
                ).intValue()
        );

        assertEquals(
                -25,
                calcDelta(
                        analysisRes(move(null, 23)),
                        analysisRes(move(null, 48)),
                        false
                ).intValue()
        );

        assertEquals(
                -25,
                calcDelta(
                        analysisRes(move(null, -23)),
                        analysisRes(move(null, -48)),
                        true
                ).intValue()
        );

        assertEquals(
                25,
                calcDelta(
                        analysisRes(move(null, -23)),
                        analysisRes(move(null, -48)),
                        false
                ).intValue()
        );

        assertEquals(
                -9999,
                calcDelta(
                        analysisRes(move(3, null)),
                        analysisRes(move(null, 658)),
                        true
                ).intValue()
        );

        assertEquals(
                -9999,
                calcDelta(
                        analysisRes(move(-3, null)),
                        analysisRes(move(null, 658)),
                        false
                ).intValue()
        );

        assertEquals(
                9999,
                calcDelta(
                        analysisRes(move(-3, null)),
                        analysisRes(move(null, 658)),
                        true
                ).intValue()
        );

        assertEquals(
                9999,
                calcDelta(
                        analysisRes(move(3, null)),
                        analysisRes(move(null, 658)),
                        false
                ).intValue()
        );

        assertEquals(
                -9999,
                calcDelta(
                        analysisRes(move(null, -276)),
                        analysisRes(move(-1, null)),
                        true
                ).intValue()
        );

        assertEquals(
                -9999,
                calcDelta(
                        analysisRes(move(null, -276)),
                        analysisRes(move(1, null)),
                        false
                ).intValue()
        );
    }

    private PositionAnalysisDto analysisRes(MoveAnalysisDto... possibleMoves) {
        return PositionAnalysisDto.builder()
                .possibleMoves(Arrays.asList(possibleMoves))
                .build();
    }
}