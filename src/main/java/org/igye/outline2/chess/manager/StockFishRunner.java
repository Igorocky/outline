package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.PieceShape;
import org.igye.outline2.common.ConsoleAppRunner;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.igye.outline2.chess.manager.ChessUtils.strCoordToInt;
import static org.igye.outline2.common.OutlineUtils.contains;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetter;

public class StockFishRunner {

    public static final Pattern BEST_MOVE_ANS_PATTERN = Pattern.compile("^bestmove ([a-h])([1-8])([a-h])([1-8])([nbrq]?).*");

    public static Move getNextMove(String runStockfishCmd, Move currPosition, int depth, int seconds) throws IOException {
        ConsoleAppRunner stockfish = new ConsoleAppRunner(runStockfishCmd);

        stockfish.readTill(contains("Stockfish"));
        stockfish.send("uci");
        stockfish.readTill(contains("uciok"));
        stockfish.send("position fen " + currPosition.toFen());
        stockfish.send("go depth " + depth + " movetime " + seconds*1000);
        Matcher matcher = stockfish.readTill(BEST_MOVE_ANS_PATTERN);
        CellCoords from = new CellCoords(strCoordToInt(matcher.group(1)), strCoordToInt(matcher.group(2)));
        CellCoords to = new CellCoords(strCoordToInt(matcher.group(3)), strCoordToInt(matcher.group(4)));
        PieceShape replacement = nullSafeGetter(
                matcher.group(5),
                s-> StringUtils.isBlank(s)?null:s.toUpperCase(),
                PieceShape::fromSymbol
        );
        Move result = currPosition.makeMove(from, to, replacement);
        stockfish.destroy();
        return result;
    }
}
