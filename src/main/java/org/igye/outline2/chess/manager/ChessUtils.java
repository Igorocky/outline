package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.exceptions.OutlineException;

import java.lang.reflect.Array;
import java.util.function.BiFunction;

public class ChessUtils {
    public static final String X_NAMES = "abcdefgh";

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

    public static String coordsToString(CellCoords coords) {
        if (coords != null) {
            return String.valueOf(X_NAMES.charAt(coords.getX())) + (coords.getY()+1);
        } else {
            return "--";
        }
    }

    public static byte[] mult(byte[] v, float[][] m) {
        if (v.length != m.length) {
            throw new OutlineException("v.length != m.length");
        }
        long[] resultL = new long[m[0].length];
        for (int i = 0; i < m[0].length; i++) {
            resultL[i] = 0;
            for (int k = 0; k < v.length; k++) {
                resultL[i] += (v[k]+128)*m[k][i];
            }
        }
        byte[] resultB = new byte[resultL.length];
        for (int i = 0; i < resultL.length; i++) {
            int intV = Math.toIntExact(resultL[i] / v.length);
            if (intV<0 || 255<intV) {
                throw new OutlineException("intV<0 || 255<intV, intV = " + intV);
            }
            resultB[i] = (byte) (intV-128);
        }
        return resultB;
    }

    public static Integer strCoordToInt(String strCoord) {
        if (strCoord == null) {
            return null;
        }
        return strCoordToInt(strCoord.charAt(0));
    }

    public static int strCoordToInt(char ch) {
        if (ch >= 97 && ch <= 104) {
            return ch - 97;
        } else if (ch >= 65 && ch <= 72) {
            return ch - 65;
        } else if (ch >= 49 && ch <= 56) {
            return ch - 49;
        } else {
            throw new OutlineException("Cannot convert " + ch + " to chess coordinate.");
        }
    }
}
