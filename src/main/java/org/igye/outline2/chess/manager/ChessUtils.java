package org.igye.outline2.chess.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ChessUtils {
    public static <T> List<List<T>> emptyBoard(int width, int height, BiFunction<Integer,Integer,T> elemSupplier) {
        List<List<T>> cells = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            cells.add(new ArrayList<>());
            for (int y = 0; y < height; y++) {
                cells.get(x).add(elemSupplier.apply(x,y));
            }
        }
        return cells;
    }

}
