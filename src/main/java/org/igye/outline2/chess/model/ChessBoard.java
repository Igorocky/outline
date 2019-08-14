package org.igye.outline2.chess.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.manager.ChessUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ChessBoard {
    private static final Character EMPTY_CELL_SYMBOL = '.';
    private List<List<Chessman>> board;

    public ChessBoard() {
        clear();
    }

    public ChessBoard(String str) {
        clear();
        if (!StringUtils.isEmpty(str)) {
            char[] chars = str.toCharArray();
            int idx = -1;
            final int height = board.get(0).size();
            final int width = board.size();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    idx++;
                    char symbol = chars[idx];
                    if (!EMPTY_CELL_SYMBOL.equals(symbol)) {
                        board.get(x).set(y, new Chessman(ChessmanType.fromSymbol(symbol)));
                    }
                }
            }
        }
    }

    public List<List<Chessman>> getBoard() {
        return board;
    }

    private void clear() {
        board = ChessUtils.emptyBoard(8,8, (x,y)->null);
    }

    public void placePiece(CellCoords coords, Chessman chessman) {
        board.get(coords.getX()).set(coords.getY(), chessman);
    }

    public Chessman getPieceAt(CellCoords coords) {
        return board.get(coords.getX()).get(coords.getY());
    }

    public Set<CellCoords> getPossibleMoves(CellCoords from) {
        Chessman chessman = getPieceAt(from);
        if (chessman.getType().getPieceShape() == PieceShape.KNIGHT) {
            return getPossibleTargetCellsForKnight(chessman.getType().getPieceColor(), from);
        } else if (chessman.getType().getPieceShape() == PieceShape.BISHOP) {
            return getPossibleTargetCellsForBishop(chessman.getType().getPieceColor(), from);
        } else if (chessman.getType().getPieceShape() == PieceShape.ROOK) {
            return getPossibleTargetCellsForRook(chessman.getType().getPieceColor(), from);
        } else if (chessman.getType().getPieceShape() == PieceShape.QUEEN) {
            return getPossibleTargetCellsForQueen(chessman.getType().getPieceColor(), from);
        } else if (chessman.getType().getPieceShape() == PieceShape.KING) {
            return getPossibleTargetCellsForKing(chessman.getType().getPieceColor(), from);
        } else {
            return Collections.emptySet();
        }
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        final int height = board.get(0).size();
        final int width = board.size();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(chessmanToString(board.get(x).get(y)));
            }
        }
        return sb.toString();
    }

    private String chessmanToString(Chessman chessman) {
        if (chessman == null) {
            return EMPTY_CELL_SYMBOL.toString();
        } else if (chessman.getType().getPieceColor().equals(ChessmanColor.WHITE)) {
            return chessman.getType().getPieceShape().getSymbol().toString().toUpperCase();
        } else {
            return chessman.getType().getPieceShape().getSymbol().toString();
        }
    }

    private Set<CellCoords> getPossibleTargetCellsForKnight(ChessmanColor color, CellCoords from) {
        return Arrays.asList(
                Pair.of(-2, 1),
                Pair.of(-2, -1),
                Pair.of(2, 1),
                Pair.of(2, -1),
                Pair.of(-1, 2),
                Pair.of(+1, 2),
                Pair.of(-1, -2),
                Pair.of(+1, -2)
        ).stream()
                .map(pair -> from.plusX(pair.getLeft()).plusY(pair.getRight()))
                .filter(dest -> canBePlacedTo(color, dest))
                .collect(Collectors.toSet());
    }

    private Set<CellCoords> getPossibleTargetCellsForBishop(ChessmanColor color, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(createRay(color, from, 1,1));
        result.addAll(createRay(color, from, 1,-1));
        result.addAll(createRay(color, from, -1,-1));
        result.addAll(createRay(color, from, -1,1));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForRook(ChessmanColor color, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(createRay(color, from, 0,1));
        result.addAll(createRay(color, from, 0,-1));
        result.addAll(createRay(color, from, 1,0));
        result.addAll(createRay(color, from, -1,0));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForQueen(ChessmanColor color, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(getPossibleTargetCellsForBishop(color, from));
        result.addAll(getPossibleTargetCellsForRook(color, from));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForKing(ChessmanColor color, CellCoords from) {
        return Arrays.asList(
                Pair.of(-1, 1),
                Pair.of(-1, 0),
                Pair.of(-1, 1),
                Pair.of(0, 1),
                Pair.of(0, -1),
                Pair.of(1, 1),
                Pair.of(1, 0),
                Pair.of(1, -1)
        ).stream()
                .map(pair -> from.plusX(pair.getLeft()).plusY(pair.getRight()))
                .filter(dest -> canBePlacedTo(color, dest))
                .collect(Collectors.toSet());
    }

    private Set<CellCoords> createRay(ChessmanColor color, CellCoords from, int dx, int dy) {
        Set<CellCoords> result = new HashSet<>();
        CellCoords to = from.plusX(dx).plusY(dy);
        while (canBePlacedTo(color, to)) {
            result.add(to);
            if (isEnemy(color, to)) {
                break;
            }
            to = to.plusX(dx).plusY(dy);
        }
        return result;
    }

    private boolean canBePlacedTo(ChessmanColor color, CellCoords to) {
        if (to.getX() < 0 || to.getX() > 7 || to.getY() < 0 || to.getY() > 7) {
            return false;
        }
        Chessman chessmanTypeAtDestination = getPieceAt(to);
        if (chessmanTypeAtDestination == null) {
            return true;
        }
        if (!chessmanTypeAtDestination.getType().getPieceColor().equals(color)) {
            return true;
        }
        return false;
    }

    private boolean isEnemy(ChessmanColor color, CellCoords at) {
        Chessman chessmanTypeAtDestination = getPieceAt(at);
        return chessmanTypeAtDestination != null && !chessmanTypeAtDestination.getType().getPieceColor().equals(color);
    }
}
