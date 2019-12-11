package org.igye.outline2.chess.manager.analyse;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionDto;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.exceptions.OutlineException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.igye.outline2.chess.manager.ChessUtils.coordsToString;

public class PgnParser {

    public static final String START_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";

    public static ParsedPgnDto parsePgn(String pgn) {
        ParsedPgnDto parsedPgnDto = new ParsedPgnDto();
        parsedPgnDto.setWPlayer(getAttrValue(pgn, "White"));
        parsedPgnDto.setBPlayer(getAttrValue(pgn, "Black"));
        parsedPgnDto.setPositions(new ArrayList<>());

        Matcher emptyLineMatcher = Pattern.compile("(\r?\n){2,}").matcher(pgn);
        emptyLineMatcher.find();
        int startPositionOfMoves = emptyLineMatcher.start();

        String moves = removeBrackets(removeCurlyBraces(pgn.substring(startPositionOfMoves)));

        String[] movesArr = moves.trim().split("(^|\\s+)[\\d]+\\.\\s+");
        for (String movePairStr : movesArr) {
            if (!StringUtils.isBlank(movePairStr)) {
                String[] singleMoves = movePairStr.split("\\s+");
                final String wMove = singleMoves[0];
                final String bMove = singleMoves.length > 1 ? singleMoves[1] : null;
                final ArrayList<PositionDto> movePair = new ArrayList<>();
                parsedPgnDto.getPositions().add(movePair);
                movePair.add(PositionDto.builder().notation(wMove).build());
                if (!(bMove == null || "1-0".equals(bMove) || "0-1".equals(bMove) || "1/2-1/2".equals(bMove))) {
                    movePair.add(PositionDto.builder().notation(bMove).build());
                }
            }
        }
        setFen(pgn, parsedPgnDto);
        return parsedPgnDto;
    }

    private static void setFen(String pgnStr, ParsedPgnDto parsedPgnDto) {
        String initialPositionFen = getAttrValue(pgnStr, "FEN");
        initialPositionFen = initialPositionFen != null ? initialPositionFen : START_POSITION_FEN;
        parsedPgnDto.setInitialPositionFen(initialPositionFen);
        Move currMove = new Move(initialPositionFen);
        for (List<PositionDto> movePair : parsedPgnDto.getPositions()) {
            for (PositionDto positionDto : movePair) {
                if ("...".equals(positionDto.getNotation()) && currMove.getColorOfWhoToMove() == ChessmanColor.BLACK) {
                    positionDto.setFen(currMove.toFen());
                    positionDto.setMove("...");
                } else {
                    currMove = currMove.makeMove(positionDto.getNotation());
                    positionDto.setFen(currMove.toFen());
                    positionDto.setMove(coordsToString(currMove.getFrom()) + coordsToString(currMove.getTo()));
                }
            }
        }
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
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
