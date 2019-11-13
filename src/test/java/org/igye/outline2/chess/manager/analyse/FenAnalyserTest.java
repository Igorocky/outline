package org.igye.outline2.chess.manager.analyse;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.MoveAnalysisDto;
import org.igye.outline2.chess.dto.PositionAnalysisDto;
import org.igye.outline2.chess.model.stockfish.CpuloadStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.CurrlineStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.CurrmoveStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.CurrmovenumberStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.DepthStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.HashfullStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.MultipvStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.NodesStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.NpsStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.PvStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.RefutationStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.SbhitsStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.ScoreStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.SelDepthStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.StockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.StringStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.TbhitsStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.TimeStockfishInfoOption;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.igye.outline2.chess.manager.analyse.FenAnalyser.compareMoves;
import static org.igye.outline2.common.OutlineUtils.listOf;
import static org.igye.outline2.common.OutlineUtils.mapToSet;
import static org.igye.outline2.common.OutlineUtils.setOf;
import static org.igye.outline2.common.OutlineUtils.toJson;
import static org.igye.outline2.controllers.OutlineTestUtils.STOCKFISH_CMD;
import static org.igye.outline2.controllers.OutlineTestUtils.move;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FenAnalyserTest {
    @Test
    public void parseLine_parses_all_commads() {
        //given
        String infoLine = "info "
                + " depth 1"
                + " seldepth 2"
                + " time 3"
                + " nodes 4"
                + " pv b5c4 d3c4 d5b4 a1e1 h8e8 g3d6"
                + " multipv 5"
                + " score cp 147"
                + " score mate 3"
                + " score lowerbound"
                + " score upperbound"
                + " currmove a3b4"
                + " currmovenumber 6"
                + " hashfull 7"
                + " nps 8"
                + " tbhits 9"
                + " sbhits 10"
                + " cpuload 11"
                + " refutation d5b4 a1e1"
                + " currline 2 d5b4"
                + " currline h8e8 g3d6"
                ;

        //when
        List<StockfishInfoOption> options = FenAnalyser.parseInfo(infoLine);

        //then
        assertEquals(1, ((DepthStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(2, ((SelDepthStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(3, ((TimeStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(4, ((NodesStockfishInfoOption)options.remove(0)).getValue());

        final PvStockfishInfoOption pv = (PvStockfishInfoOption) options.remove(0);
        assertEquals("b5c4 d3c4 d5b4 a1e1 h8e8 g3d6", StringUtils.join(pv.getMoves(), " "));

        assertEquals(5, ((MultipvStockfishInfoOption)options.remove(0)).getValue());

        final ScoreStockfishInfoOption scoreCp = (ScoreStockfishInfoOption) options.remove(0);
        assertEquals(147, scoreCp.getCp().longValue());
        assertNull(scoreCp.getMate());
        assertNull(scoreCp.getLowerbound());
        assertNull(scoreCp.getUpperbound());

        final ScoreStockfishInfoOption scoreMate = (ScoreStockfishInfoOption) options.remove(0);
        assertEquals(3, scoreMate.getMate().longValue());
        assertNull(scoreMate.getCp());
        assertNull(scoreMate.getLowerbound());
        assertNull(scoreMate.getUpperbound());

        final ScoreStockfishInfoOption scoreLowerbound = (ScoreStockfishInfoOption) options.remove(0);
        assertTrue(scoreLowerbound.getLowerbound());
        assertNull(scoreLowerbound.getCp());
        assertNull(scoreLowerbound.getMate());
        assertNull(scoreLowerbound.getUpperbound());

        final ScoreStockfishInfoOption scoreUpperbound = (ScoreStockfishInfoOption) options.remove(0);
        assertTrue(scoreUpperbound.getUpperbound());
        assertNull(scoreUpperbound.getCp());
        assertNull(scoreUpperbound.getMate());
        assertNull(scoreUpperbound.getLowerbound());

        assertEquals("a3b4", ((CurrmoveStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(6, ((CurrmovenumberStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(7, ((HashfullStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(8, ((NpsStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(9, ((TbhitsStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(10, ((SbhitsStockfishInfoOption)options.remove(0)).getValue());
        assertEquals(11, ((CpuloadStockfishInfoOption)options.remove(0)).getValue());

        final RefutationStockfishInfoOption refutation = (RefutationStockfishInfoOption) options.remove(0);
        assertEquals("d5b4 a1e1", StringUtils.join(refutation.getMoves(), " "));

        final CurrlineStockfishInfoOption curLine1 = (CurrlineStockfishInfoOption) options.remove(0);
        assertEquals(2, curLine1.getCpuNum().longValue());
        assertEquals("d5b4", StringUtils.join(curLine1.getMoves(), " "));

        final CurrlineStockfishInfoOption curLine2 = (CurrlineStockfishInfoOption) options.remove(0);
        assertNull(curLine2.getCpuNum());
        assertEquals("h8e8 g3d6", StringUtils.join(curLine2.getMoves(), " "));

        assertTrue(options.isEmpty());
    }

    @Test
    public void parseLine_parses_lowerbound() {
        //given
        String infoLine = "info multipv 1 score cp 46 lowerbound tbhits 0";

        //when
        List<StockfishInfoOption> options = FenAnalyser.parseInfo(infoLine);

        //then
        assertEquals(1, ((MultipvStockfishInfoOption)options.remove(0)).getValue());

        final ScoreStockfishInfoOption scoreCp = (ScoreStockfishInfoOption) options.remove(0);
        assertEquals(46, scoreCp.getCp().longValue());
        assertTrue(scoreCp.getLowerbound());
        assertNull(scoreCp.getUpperbound());

        assertEquals(0, ((TbhitsStockfishInfoOption)options.remove(0)).getValue());

        assertTrue(options.isEmpty());
    }

    @Test
    public void parseLine_parses_upperbound() {
        //given
        String infoLine = "info multipv 1 score cp 46 upperbound tbhits 0";

        //when
        List<StockfishInfoOption> options = FenAnalyser.parseInfo(infoLine);

        //then
        assertEquals(1, ((MultipvStockfishInfoOption)options.remove(0)).getValue());

        final ScoreStockfishInfoOption scoreCp = (ScoreStockfishInfoOption) options.remove(0);
        assertEquals(46, scoreCp.getCp().longValue());
        assertTrue(scoreCp.getUpperbound());
        assertNull(scoreCp.getLowerbound());

        assertEquals(0, ((TbhitsStockfishInfoOption)options.remove(0)).getValue());

        assertTrue(options.isEmpty());
    }

    @Test
    public void parseLine_parses_string_command() {
        //given
        String infoLine = "info string asg gf sdf sdf d";

        //when
        List<StockfishInfoOption> options = FenAnalyser.parseInfo(infoLine);

        //then
        assertEquals("asg", ((StringStockfishInfoOption)options.remove(0)).getValue());
        assertTrue(options.isEmpty());
    }

    @Test
    public void analyseFen_analyses_FEN() throws IOException {
        //given
        try (FenAnalyser fenAnalyser = new FenAnalyser(STOCKFISH_CMD)) {

            //when
            PositionAnalysisDto positionAnalysisDto = fenAnalyser.analyseFen(
                    "rn3rk1/p4ppp/2p4n/8/1p1q1P2/1P1P2P1/P1PQR1BP/2KR4 w - - 0 18",
                    15,
                    null,
                    null
            );

            //then
            System.out.println("positionAnalysisDto = " + toJson(positionAnalysisDto));
            Assert.assertEquals("c1b1", positionAnalysisDto.getPossibleMoves().get(0).getMove());
            assertEquals(
                    setOf("c1b1","d2e1","c2c3","d2e3","c2c4"),
                    mapToSet(positionAnalysisDto.getPossibleMoves(), MoveAnalysisDto::getMove)
            );
        }
    }

    @Test
    public void compareMoves_compares_moves_correctly() {
        MoveAnalysisDto m1 = move(null, 58);
        MoveAnalysisDto m2 = move(null, 65);
        assertTrue(compareMoves(m1,m2) == -compareMoves(m2,m1));
        assertTrue(compareMoves(m1,m2) == -compareMoves(m2,m1));

        m1 = move(null, -150);
        m2 = move(null, -150);
        assertTrue(compareMoves(m1,m2) == -compareMoves(m2,m1));
        assertTrue(compareMoves(m1,m2) == -compareMoves(m2,m1));

        assertMovesOrder(move(1,null), move(3,null));
        assertMovesOrder(move(-3,null), move(-1,null));
        assertMovesOrder(move(2,null), move(-3,null));
        assertMovesOrder(move(2,null), move(null,5));
        assertMovesOrder(move(null,5), move(-2,null));
        assertMovesOrder(move(null,7), move(null,4));
    }

    private void assertMovesOrder(MoveAnalysisDto m1, MoveAnalysisDto m2) {
        ArrayList<MoveAnalysisDto> moves1 = new ArrayList<>(listOf(m1,m2));
        Collections.sort(moves1, FenAnalyser::compareMoves);
        assertTrue(moves1.get(0) == m1);

        ArrayList<MoveAnalysisDto> moves2 = new ArrayList<>(listOf(m2,m1));
        Collections.sort(moves2, FenAnalyser::compareMoves);
        assertTrue(moves2.get(0) == m1);
    }
}