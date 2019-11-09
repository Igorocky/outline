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
}