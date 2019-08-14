package org.igye.outline2.chess.model;

import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.manager.ChessUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ChessBoard {
    private List<List<ChessmanType>> board;

    public ChessBoard() {
        clear();
    }

    public List<List<ChessmanType>> getBoard() {
        return board;
    }

    public void clear() {
        board = ChessUtils.emptyBoard(8,8, (x,y)->null);
    }

    public void placePiece(CellCoords coords, ChessmanType chessmanType) {
        board.get(coords.getX()).set(coords.getY(), chessmanType);
    }

    public ChessmanType getPieceAt(CellCoords coords) {
        return board.get(coords.getX()).get(coords.getY());
    }

    public Set<CellCoords> getPossibleMoves(CellCoords from) {
        ChessmanType chessmanType = getPieceAt(from);
        if (chessmanType.getPieceShape() == PieceShape.KNIGHT) {
            return getPossibleTargetCellsForKnight(chessmanType.getPieceColor(), from);
        } else if (chessmanType.getPieceShape() == PieceShape.BISHOP) {
            return getPossibleTargetCellsForBishop(chessmanType.getPieceColor(), from);
        } else if (chessmanType.getPieceShape() == PieceShape.ROOK) {
            return getPossibleTargetCellsForRook(chessmanType.getPieceColor(), from);
        } else if (chessmanType.getPieceShape() == PieceShape.QUEEN) {
            return getPossibleTargetCellsForQueen(chessmanType.getPieceColor(), from);
        } else if (chessmanType.getPieceShape() == PieceShape.KING) {
            return getPossibleTargetCellsForKing(chessmanType.getPieceColor(), from);
        } else {
            return Collections.emptySet();
        }
    }

    private Set<CellCoords> getPossibleTargetCellsForKnight(PieceColor color, CellCoords from) {
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

    private Set<CellCoords> getPossibleTargetCellsForBishop(PieceColor color, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(createRay(color, from, 1,1));
        result.addAll(createRay(color, from, 1,-1));
        result.addAll(createRay(color, from, -1,-1));
        result.addAll(createRay(color, from, -1,1));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForRook(PieceColor color, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(createRay(color, from, 0,1));
        result.addAll(createRay(color, from, 0,-1));
        result.addAll(createRay(color, from, 1,0));
        result.addAll(createRay(color, from, -1,0));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForQueen(PieceColor color, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(getPossibleTargetCellsForBishop(color, from));
        result.addAll(getPossibleTargetCellsForRook(color, from));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForKing(PieceColor color, CellCoords from) {
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

    private Set<CellCoords> createRay(PieceColor color, CellCoords from, int dx, int dy) {
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

    private boolean canBePlacedTo(PieceColor color, CellCoords to) {
        if (to.getX() < 0 || to.getX() > 7 || to.getY() < 0 || to.getY() > 7) {
            return false;
        }
        ChessmanType chessmanTypeAtDestination = getPieceAt(to);
        if (chessmanTypeAtDestination == null) {
            return true;
        }
        if (!chessmanTypeAtDestination.getPieceColor().equals(color)) {
            return true;
        }
        return false;
    }

    private boolean isEnemy(PieceColor color, CellCoords at) {
        ChessmanType chessmanTypeAtDestination = getPieceAt(at);
        return chessmanTypeAtDestination != null && !chessmanTypeAtDestination.getPieceColor().equals(color);
    }

    private void leaveAvailableCells(PieceColor color, Set<CellCoords> cells) {
        cells.removeIf(cell -> !canBePlacedTo(color, cell));
    }
}
