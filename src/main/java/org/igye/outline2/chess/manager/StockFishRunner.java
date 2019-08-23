package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.PieceShape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.igye.outline2.chess.manager.ChessUtils.strCoordToInt;

public class StockFishRunner {
    public static Move getNextMove(String runStockfishCmd, Move currPosition, int depth, int seconds) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(runStockfishCmd);
        Process proc = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        readTill(reader, contains("Stockfish"));
        sendText(proc, "uci");
        readTill(reader, contains("uciok"));
        sendText(proc, "position fen " + currPosition.toFen());
        sendText(proc, "go depth " + depth + " movetime " + seconds*1000);
        Matcher matcher = readTill(reader, Pattern.compile("^bestmove ([a-h])([1-8])([a-h])([1-8])([nbrq]?).*")).getRight();
        CellCoords from = new CellCoords(strCoordToInt(matcher.group(1)), strCoordToInt(matcher.group(2)));
        CellCoords to = new CellCoords(strCoordToInt(matcher.group(3)), strCoordToInt(matcher.group(4)));
        PieceShape replacement = nullSafeGetter(
                matcher.group(5),
                s-> StringUtils.isBlank(s)?null:s.toUpperCase(),
                PieceShape::fromSymbol
        );
        Move result = currPosition.makeMove(from, to, replacement);
        proc.destroy();
        return result;
    }

    private static Pattern contains(String subStr) {
        return Pattern.compile(".*" + subStr + ".*");
    }

    private static void sendText(Process proc, String text) throws IOException {
//        System.out.println("<<<<< " + text);
        proc.getOutputStream().write(text.getBytes());
        proc.getOutputStream().write("\n".getBytes());
        proc.getOutputStream().flush();
    }

    private static Pair<List<String>, Matcher> readTill(BufferedReader reader, Pattern pattern) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = reader.readLine();
//        System.out.println(">>>>> " + line);
        lines.add(line);
        Matcher matcher = pattern.matcher(line);
        while (!matcher.matches()) {
            line = reader.readLine();
//            System.out.println(">>>>> " + line);
            lines.add(line);
            matcher = pattern.matcher(line);
        }
        return Pair.of(lines, matcher);
    }
}
