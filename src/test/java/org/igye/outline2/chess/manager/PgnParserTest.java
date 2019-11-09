package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ParsedPgnDto;
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
        assertEquals("e4", parsedPgnDto.getMoves().get(0).get(0).getNotation());
        assertEquals("O-O", parsedPgnDto.getMoves().get(5).get(1).getNotation());
        assertEquals("O-O-O", parsedPgnDto.getMoves().get(7).get(0).getNotation());
        assertEquals("Nxf6+", parsedPgnDto.getMoves().get(10).get(0).getNotation());
        assertEquals("Qe7", parsedPgnDto.getMoves().get(19).get(1).getNotation());
    }
}