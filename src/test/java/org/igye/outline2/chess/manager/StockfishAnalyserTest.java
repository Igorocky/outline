package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
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
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class StockfishAnalyserTest {
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
        List<StockfishInfoOption> options = StockfishAnalyser.parseInfo(infoLine);

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

        final ScoreStockfishInfoOption scoreMate = (ScoreStockfishInfoOption) options.remove(0);
        assertEquals(3, scoreMate.getMate().longValue());

        assertTrue(((ScoreStockfishInfoOption)options.remove(0)).getLowerbound());
        assertTrue(((ScoreStockfishInfoOption)options.remove(0)).getUpperbound());
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
    public void parseLine_parses_string_command() {
        //given
        String infoLine = "info string asg gf sdf sdf d";

        //when
        List<StockfishInfoOption> options = StockfishAnalyser.parseInfo(infoLine);

        //then
        assertEquals("asg", ((StringStockfishInfoOption)options.remove(0)).getValue());
        assertTrue(options.isEmpty());
    }
}