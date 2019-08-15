package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.model.*;

public class ChessBoardBuilder {
    private ChessBoard chessBoard;

    public ChessBoardBuilder() {
        chessBoard = new ChessBoard();
    }

    public ChessBoard build() {
        return chessBoard;
    }

    public ChessBoardBuilder P(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_PAWN)); return this; }
    public ChessBoardBuilder N(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_KNIGHT)); return this; }
    public ChessBoardBuilder B(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_BISHOP)); return this; }
    public ChessBoardBuilder R(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_ROOK)); return this; }
    public ChessBoardBuilder Q(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_QUEEN)); return this; }
    public ChessBoardBuilder K(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_KING)); return this; }
    public ChessBoardBuilder p(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_PAWN)); return this; }
    public ChessBoardBuilder n(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_KNIGHT)); return this; }
    public ChessBoardBuilder b(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_BISHOP)); return this; }
    public ChessBoardBuilder r(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_ROOK)); return this; }
    public ChessBoardBuilder q(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_QUEEN)); return this; }
    public ChessBoardBuilder k(CellCoords coords) { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_KING)); return this; }
}
