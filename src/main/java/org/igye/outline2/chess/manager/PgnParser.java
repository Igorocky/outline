package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ParsedPgnDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PgnParser {
    public static ParsedPgnDto parsePgn(String pgn) {
        ParsedPgnDto parsedPgnDto = new ParsedPgnDto();
        parsedPgnDto.setWPlayer(getAttrValue(pgn, "White"));
        parsedPgnDto.setBPlayer(getAttrValue(pgn, "Black"));
        return parsedPgnDto;
    }

    private static String getAttrValue(String pgn, String attrName) {
        final String regex = "\\[" + attrName + "\\s\"([^\"]*)\"\\]";
        final Matcher matcher = Pattern.compile(regex).matcher(pgn);
        matcher.find();
        return matcher.group(1);
    }
}
