package org.igye.outline2.chess.model;

import org.igye.outline2.chess.manager.ChessUtils;

import java.util.List;

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
}
