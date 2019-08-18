package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.Move;

import java.lang.reflect.Array;
import java.util.function.BiFunction;

public class ChessUtils {
    private static final String X_NAMES = "abcdefgh";

    public static <T> T[][] emptyBoard(Class<T[]> clazz1, Class<T> clazz2,
                                       int width, int height, BiFunction<Integer,Integer,T> elemSupplier) {
        T[][] cells = (T[][]) Array.newInstance(clazz1, width);
        for (int x = 0; x < width; x++) {
            cells[x] = (T[]) Array.newInstance(clazz2, height);
            for (int y = 0; y < height; y++) {
                cells[x][y] = elemSupplier.apply(x,y);
            }
        }
        return cells;
    }

    public static String moveToString(Move move) {
        return coordsToString(move.getFrom()) + "-" + coordsToString(move.getTo());
    }

    public static String coordsToString(CellCoords coords) {
        if (coords != null) {
            return String.valueOf(X_NAMES.charAt(coords.getX())) + (coords.getY()+1);
        } else {
            return "--";
        }
    }

}
