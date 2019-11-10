package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.MoveDto;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.exceptions.OutlineException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PgnParser {

    public static final String START_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";

    public static ParsedPgnDto parsePgn(String pgn) {
        ParsedPgnDto parsedPgnDto = new ParsedPgnDto();
        parsedPgnDto.setWPlayer(getAttrValue(pgn, "White"));
        parsedPgnDto.setBPlayer(getAttrValue(pgn, "Black"));
        parsedPgnDto.setMoves(new ArrayList<>());

        Matcher emptyLineMatcher = Pattern.compile("(\r?\n){2,}").matcher(pgn);
        emptyLineMatcher.find();
        int startPositionOfMoves = emptyLineMatcher.start();

        String moves = removeBrackets(removeCurlyBraces(pgn.substring(startPositionOfMoves)));

        String[] movesArr = moves.trim().split("(^|\\s+)[\\d]+\\.\\s+");
        for (String movePairStr : movesArr) {
            if (!StringUtils.isBlank(movePairStr)) {
                String[] singleMoves = movePairStr.split("\\s+");
                parsedPgnDto.getMoves().add(Arrays.asList(
                        MoveDto.builder().notation(singleMoves[0]).build(),
                        MoveDto.builder().notation(singleMoves[1]).build()
                ));
            }
        }
        setFen(parsedPgnDto);
        return parsedPgnDto;
    }

    private static void setFen(ParsedPgnDto parsedPgnDto) {
        Move currMove = new Move(new CellCoords(0,7), new ChessBoard(START_POSITION_FEN));
        for (List<MoveDto> movePair : parsedPgnDto.getMoves()) {
            for (MoveDto moveDto : movePair) {
                currMove = currMove.makeMove(moveDto.getNotation());
                moveDto.setFen(currMove.toFen());
                moveDto.setCellFrom(cellCoordsToAbsNum(currMove.getFrom()));
                moveDto.setCellTo(cellCoordsToAbsNum(currMove.getTo()));
            }
        }
    }

    private static int cellCoordsToAbsNum(CellCoords coords) {
        return coords.getY()*8 + coords.getX();
    }

    private static String removeCurlyBraces(String pgn) {
        return pgn.replaceAll("\\{[^\\}]*\\}", "");
    }

    private static String removeBrackets(String pgn) {
        StringBuilder sb = new StringBuilder();
        int brCnt = 0;
        int idx = 0;
        while (idx < pgn.length()) {
            char ch = pgn.charAt(idx);
            if ('(' == ch) {
                brCnt++;
            } else if (')' == ch) {
                brCnt--;
                if (brCnt < 0) {
                    throw new OutlineException("brCnt < 0");
                }
            } else if (brCnt == 0) {
                sb.append(ch);
            }
            idx++;
        }
        return sb.toString();
    }

    private static String getAttrValue(String pgn, String attrName) {
        final String regex = "\\[" + attrName + "\\s\"([^\"]*)\"\\]";
        final Matcher matcher = Pattern.compile(regex).matcher(pgn);
        matcher.find();
        return matcher.group(1);
    }
}
