package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanType;

public class ChessBoardBuilder {
    private ChessBoard chessBoard;

    public ChessBoardBuilder() {
        chessBoard = new ChessBoard();
    }

    public ChessBoard build() {
        return chessBoard;
    }

    public ChessBoardBuilder P(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.WHITE_PAWN); return this; }
    public ChessBoardBuilder N(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.WHITE_KNIGHT); return this; }
    public ChessBoardBuilder B(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.WHITE_BISHOP); return this; }
    public ChessBoardBuilder R(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.WHITE_ROOK); return this; }
    public ChessBoardBuilder Q(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.WHITE_QUEEN); return this; }
    public ChessBoardBuilder K(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.WHITE_KING); return this; }
    public ChessBoardBuilder p(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.BLACK_PAWN); return this; }
    public ChessBoardBuilder n(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.BLACK_KNIGHT); return this; }
    public ChessBoardBuilder b(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.BLACK_BISHOP); return this; }
    public ChessBoardBuilder r(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.BLACK_ROOK); return this; }
    public ChessBoardBuilder q(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.BLACK_QUEEN); return this; }
    public ChessBoardBuilder k(CellCoords coords) { chessBoard.placePiece(coords, ChessmanType.BLACK_KING); return this; }
    public ChessBoardBuilder _(CellCoords coords) { chessBoard.placePiece(coords, null); return this; }
}
