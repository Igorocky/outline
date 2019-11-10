package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.manager.analyse.PgnParser;
import org.igye.outline2.common.OutlineUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PgnParserTest {
    @Test
    public void parsePgn_determines_players_names() throws IOException {
        //given
        String pgnStr = OutlineUtils.readStringFromClasspath("/test-data/pgn/test-pgn1.pgn");

        //when
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgnStr);

        //then
        assertEquals("player111", parsedPgnDto.getWPlayer());
        assertEquals("player222", parsedPgnDto.getBPlayer());
    }

    @Test
    public void parsePgn_parses_all_moves() throws IOException {
        //given
        String pgnStr = OutlineUtils.readStringFromClasspath("/test-data/pgn/test-pgn1.pgn");

        //when
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgnStr);

        //then
        assertEquals("e4", parsedPgnDto.getPositions().get(0).get(0).getNotation());
        assertEquals("O-O", parsedPgnDto.getPositions().get(5).get(1).getNotation());
        assertEquals("O-O-O", parsedPgnDto.getPositions().get(7).get(0).getNotation());
        assertEquals("Nxf6+", parsedPgnDto.getPositions().get(10).get(0).getNotation());
        assertEquals("Qe7", parsedPgnDto.getPositions().get(19).get(1).getNotation());
    }

    @Test
    public void parsePgn_sets_cellFrom_and_cellTo() throws IOException {
        //given
        String pgnStr = OutlineUtils.readStringFromClasspath("/test-data/pgn/test-pgn1.pgn");

        //when
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgnStr);

        //then
        assertEquals("e2e4", parsedPgnDto.getPositions().get(0).get(0).getMove());
        assertEquals("e8g8", parsedPgnDto.getPositions().get(5).get(1).getMove());
        assertEquals("f6e7", parsedPgnDto.getPositions().get(19).get(1).getMove());
    }

    @Test
    public void parsePgn_sets_FEN() throws IOException {
        //given
        String pgnStr = OutlineUtils.readStringFromClasspath("/test-data/pgn/test-pgn1.pgn");

        //when
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgnStr);

        //then
        assertEquals(
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
                parsedPgnDto.getPositions().get(0).get(0).getFen()
        );
        assertEquals(
                "r1bqk2r/pppp1ppp/2n2n2/2b5/4P3/2N5/PPPQ1PPP/R1B1KBNR w KQkq - 5 6",
                parsedPgnDto.getPositions().get(4).get(1).getFen()
        );
        assertEquals(
                "r1bq1rk1/pppp1ppp/2n2n2/2b5/4P3/2N5/PPP1QPPP/R1B1KBNR w KQ - 7 7",
                parsedPgnDto.getPositions().get(5).get(1).getFen()
        );
        assertEquals(
                "r1bq1rk1/pppp1ppp/2n2n2/2b5/4P3/2N1B3/PPP1QPPP/R3KBNR b KQ - 8 7",
                parsedPgnDto.getPositions().get(6).get(0).getFen()
        );
        assertEquals(
                "r1bq1rk1/pppp1ppp/2n2n2/8/1b2P3/2N1B3/PPP1QPPP/2KR1BNR b - - 10 8",
                parsedPgnDto.getPositions().get(7).get(0).getFen()
        );
        assertEquals(
                "r3r1k1/ppp2ppp/2n1bq2/8/1b2P3/4BP2/PPP1Q1PP/2KR1BNR w - - 0 12",
                parsedPgnDto.getPositions().get(10).get(1).getFen()
        );
        assertEquals(
                "r3r1k1/1pp1qppp/1p2b3/4P3/1P5P/1P3P1N/2P1Q1P1/1K1R1B1R w - - 1 21",
                parsedPgnDto.getPositions().get(19).get(1).getFen()
        );
    }

    @Test
    public void parsePgn_doesnt_fail_when_white_wins() throws IOException {
        //given
        String pgnStr = OutlineUtils.readStringFromClasspath("/test-data/pgn/white-wins.pgn");

        //when
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgnStr);

        //then
        assertEquals(1, parsedPgnDto.getPositions().get(28).size());
        assertEquals(
                "6k1/1p2qppp/1R6/8/3pPPb1/r7/2BK2PP/7R b - - 4 29",
                parsedPgnDto.getPositions().get(28).get(0).getFen()
        );
    }
}