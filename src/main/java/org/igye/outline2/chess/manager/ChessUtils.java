package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.igye.outline2.exceptions.OutlineException;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KING;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_ROOK;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KING;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_ROOK;

public class ChessUtils {
    public static final String X_NAMES = "abcdefgh";
    public static final Comparator<CellCoords> WHITE_SIDE_CELL_COMPARATOR =
            Comparator.comparingInt(c -> (c.getX() * 8 + c.getY()));
    public static final Comparator<CellCoords> BLACK_SIDE_CELL_COMPARATOR =
            Comparator.comparingInt(c -> -(c.getX() * 8 + c.getY()));

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

    public static String renderTextChessboard(ChessBoard position, ChessmanColor whoToMove) {
        StringBuilder sb = new StringBuilder();
        if (whoToMove == WHITE) {
            renderBlackPieces(position, WHITE_SIDE_CELL_COMPARATOR, sb);
            sb.append("\n");
            renderWhitePieces(position, WHITE_SIDE_CELL_COMPARATOR, sb);
        } else {
            renderWhitePieces(position, BLACK_SIDE_CELL_COMPARATOR, sb);
            sb.append("\n");
            renderBlackPieces(position, BLACK_SIDE_CELL_COMPARATOR, sb);
        }
        return sb.toString();
    }

    private static void renderWhitePieces(ChessBoard position,
                                          Comparator<CellCoords> cellComparator, StringBuilder sb) {
        sb.append("White:");
        addLocationsOf(position, cellComparator, sb, "P", WHITE_PAWN);
        addLocationsOf(position, cellComparator, sb, "N", WHITE_KNIGHT);
        addLocationsOf(position, cellComparator, sb, "B", WHITE_BISHOP);
        addLocationsOf(position, cellComparator, sb, "R", WHITE_ROOK);
        addLocationsOf(position, cellComparator, sb, "Q", WHITE_QUEEN);
        addLocationsOf(position, cellComparator, sb, "K", WHITE_KING);
    }

    private static void renderBlackPieces(ChessBoard position,
                                          Comparator<CellCoords> cellComparator, StringBuilder sb) {
        sb.append("Black:");
        addLocationsOf(position, cellComparator, sb, "p", BLACK_PAWN);
        addLocationsOf(position, cellComparator, sb, "n", BLACK_KNIGHT);
        addLocationsOf(position, cellComparator, sb, "b", BLACK_BISHOP);
        addLocationsOf(position, cellComparator, sb, "r", BLACK_ROOK);
        addLocationsOf(position, cellComparator, sb, "q", BLACK_QUEEN);
        addLocationsOf(position, cellComparator, sb, "k", BLACK_KING);
    }

    private static void addLocationsOf(ChessBoard position,
                                       Comparator<CellCoords> cellComparator,
                                StringBuilder sb, String prefix, ChessmanType chessmanType) {
        sb.append("\n").append(prefix).append(":");
        findLocationsOf(position, cellComparator, chessmanType)
                .forEach(cell -> sb.append(" ").append(ChessUtils.coordsToString(cell)));
    }

    private static Stream<CellCoords> findLocationsOf(
            ChessBoard position,
            Comparator<CellCoords> cellComparator,
            ChessmanType chessmanType) {
        return position.findAll(ct -> ct == chessmanType).stream().sorted(cellComparator);
    }

}
