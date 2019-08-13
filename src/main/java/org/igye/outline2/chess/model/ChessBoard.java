package org.igye.outline2.chess.model;

import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.manager.ChessUtils;
import org.igye.outline2.exceptions.OutlineException;

import java.util.*;
import java.util.stream.Collectors;

import static org.igye.outline2.chess.model.PieceColor.BLACK;
import static org.igye.outline2.chess.model.PieceColor.WHITE;

public class ChessBoard {
    private List<List<Piece>> board;

    public ChessBoard() {
        clear();
    }

    public List<List<Piece>> getBoard() {
        return board;
    }

    public void clear() {
        board = ChessUtils.emptyBoard(8,8, (x,y)->null);
    }

    public void placePiece(CellCoords coords, Piece piece) {
        board.get(coords.getX()).set(coords.getY(), piece);
    }

    public Piece getPieceAt(CellCoords coords) {
        return board.get(coords.getX()).get(coords.getY());
    }

    public Set<CellCoords> getPossibleMoves(CellCoords from) {
        Piece piece = getPieceAt(from);
        if (piece.getPieceShape() == PieceShape.KNIGHT) {
            return getPossibleTargetCellsForKnight(piece.getPieceColor(), from);
        } else if (piece.getPieceShape() == PieceShape.BISHOP) {
            return getPossibleTargetCellsForBishop(piece.getPieceColor(), from);
        } else if (piece.getPieceShape() == PieceShape.ROOK) {
            return getPossibleTargetCellsForRook(piece.getPieceColor(), from);
        } else if (piece.getPieceShape() == PieceShape.QUEEN) {
            return getPossibleTargetCellsForQueen(piece.getPieceColor(), from);
        } else if (piece.getPieceShape() == PieceShape.KING) {
            return getPossibleTargetCellsForKing(piece.getPieceColor(), from);
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
        Piece pieceAtDestination = getPieceAt(to);
        if (pieceAtDestination == null) {
            return true;
        }
        if (!pieceAtDestination.getPieceColor().equals(color)) {
            return true;
        }
        return false;
    }

    private boolean isEnemy(PieceColor color, CellCoords at) {
        Piece pieceAtDestination = getPieceAt(at);
        return pieceAtDestination != null && !pieceAtDestination.getPieceColor().equals(color);
    }

    private void leaveAvailableCells(PieceColor color, Set<CellCoords> cells) {
        cells.removeIf(cell -> !canBePlacedTo(color, cell));
    }
}
