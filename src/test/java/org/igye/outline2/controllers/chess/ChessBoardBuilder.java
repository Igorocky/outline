package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.model.*;

public class ChessBoardBuilder {
    private ChessBoard chessBoard;
    private CellCoords coords;

    public ChessBoardBuilder() {
        chessBoard = new ChessBoard();
    }

    public ChessBoard build() {
        return chessBoard;
    }

    public ChessBoardBuilder whitePawn() { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_PAWN)); return this; }
    public ChessBoardBuilder whiteKnight() { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_KNIGHT)); return this; }
    public ChessBoardBuilder whiteBishop() { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_BISHOP)); return this; }
    public ChessBoardBuilder whiteRook() { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_ROOK)); return this; }
    public ChessBoardBuilder whiteQueen() { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_QUEEN)); return this; }
    public ChessBoardBuilder whiteKing() { chessBoard.placePiece(coords, new Chessman(ChessmanType.WHITE_KING)); return this; }
    public ChessBoardBuilder blackPawn() { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_PAWN)); return this; }
    public ChessBoardBuilder blackKnight() { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_KNIGHT)); return this; }
    public ChessBoardBuilder blackBishop() { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_BISHOP)); return this; }
    public ChessBoardBuilder blackRook() { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_ROOK)); return this; }
    public ChessBoardBuilder blackQueen() { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_QUEEN)); return this; }
    public ChessBoardBuilder blackKing() { chessBoard.placePiece(coords, new Chessman(ChessmanType.BLACK_KING)); return this; }

    public ChessBoardBuilder a1() { coords = new CellCoords(0,0); return this; }
    public ChessBoardBuilder b1() { coords = new CellCoords(1,0); return this; }
    public ChessBoardBuilder c1() { coords = new CellCoords(2,0); return this; }
    public ChessBoardBuilder d1() { coords = new CellCoords(3,0); return this; }
    public ChessBoardBuilder e1() { coords = new CellCoords(4,0); return this; }
    public ChessBoardBuilder f1() { coords = new CellCoords(5,0); return this; }
    public ChessBoardBuilder g1() { coords = new CellCoords(6,0); return this; }
    public ChessBoardBuilder h1() { coords = new CellCoords(7,0); return this; }
    public ChessBoardBuilder a2() { coords = new CellCoords(0,1); return this; }
    public ChessBoardBuilder b2() { coords = new CellCoords(1,1); return this; }
    public ChessBoardBuilder c2() { coords = new CellCoords(2,1); return this; }
    public ChessBoardBuilder d2() { coords = new CellCoords(3,1); return this; }
    public ChessBoardBuilder e2() { coords = new CellCoords(4,1); return this; }
    public ChessBoardBuilder f2() { coords = new CellCoords(5,1); return this; }
    public ChessBoardBuilder g2() { coords = new CellCoords(6,1); return this; }
    public ChessBoardBuilder h2() { coords = new CellCoords(7,1); return this; }
    public ChessBoardBuilder a3() { coords = new CellCoords(0,2); return this; }
    public ChessBoardBuilder b3() { coords = new CellCoords(1,2); return this; }
    public ChessBoardBuilder c3() { coords = new CellCoords(2,2); return this; }
    public ChessBoardBuilder d3() { coords = new CellCoords(3,2); return this; }
    public ChessBoardBuilder e3() { coords = new CellCoords(4,2); return this; }
    public ChessBoardBuilder f3() { coords = new CellCoords(5,2); return this; }
    public ChessBoardBuilder g3() { coords = new CellCoords(6,2); return this; }
    public ChessBoardBuilder h3() { coords = new CellCoords(7,2); return this; }
    public ChessBoardBuilder a4() { coords = new CellCoords(0,3); return this; }
    public ChessBoardBuilder b4() { coords = new CellCoords(1,3); return this; }
    public ChessBoardBuilder c4() { coords = new CellCoords(2,3); return this; }
    public ChessBoardBuilder d4() { coords = new CellCoords(3,3); return this; }
    public ChessBoardBuilder e4() { coords = new CellCoords(4,3); return this; }
    public ChessBoardBuilder f4() { coords = new CellCoords(5,3); return this; }
    public ChessBoardBuilder g4() { coords = new CellCoords(6,3); return this; }
    public ChessBoardBuilder h4() { coords = new CellCoords(7,3); return this; }
    public ChessBoardBuilder a5() { coords = new CellCoords(0,4); return this; }
    public ChessBoardBuilder b5() { coords = new CellCoords(1,4); return this; }
    public ChessBoardBuilder c5() { coords = new CellCoords(2,4); return this; }
    public ChessBoardBuilder d5() { coords = new CellCoords(3,4); return this; }
    public ChessBoardBuilder e5() { coords = new CellCoords(4,4); return this; }
    public ChessBoardBuilder f5() { coords = new CellCoords(5,4); return this; }
    public ChessBoardBuilder g5() { coords = new CellCoords(6,4); return this; }
    public ChessBoardBuilder h5() { coords = new CellCoords(7,4); return this; }
    public ChessBoardBuilder a6() { coords = new CellCoords(0,5); return this; }
    public ChessBoardBuilder b6() { coords = new CellCoords(1,5); return this; }
    public ChessBoardBuilder c6() { coords = new CellCoords(2,5); return this; }
    public ChessBoardBuilder d6() { coords = new CellCoords(3,5); return this; }
    public ChessBoardBuilder e6() { coords = new CellCoords(4,5); return this; }
    public ChessBoardBuilder f6() { coords = new CellCoords(5,5); return this; }
    public ChessBoardBuilder g6() { coords = new CellCoords(6,5); return this; }
    public ChessBoardBuilder h6() { coords = new CellCoords(7,5); return this; }
    public ChessBoardBuilder a7() { coords = new CellCoords(0,6); return this; }
    public ChessBoardBuilder b7() { coords = new CellCoords(1,6); return this; }
    public ChessBoardBuilder c7() { coords = new CellCoords(2,6); return this; }
    public ChessBoardBuilder d7() { coords = new CellCoords(3,6); return this; }
    public ChessBoardBuilder e7() { coords = new CellCoords(4,6); return this; }
    public ChessBoardBuilder f7() { coords = new CellCoords(5,6); return this; }
    public ChessBoardBuilder g7() { coords = new CellCoords(6,6); return this; }
    public ChessBoardBuilder h7() { coords = new CellCoords(7,6); return this; }
    public ChessBoardBuilder a8() { coords = new CellCoords(0,7); return this; }
    public ChessBoardBuilder b8() { coords = new CellCoords(1,7); return this; }
    public ChessBoardBuilder c8() { coords = new CellCoords(2,7); return this; }
    public ChessBoardBuilder d8() { coords = new CellCoords(3,7); return this; }
    public ChessBoardBuilder e8() { coords = new CellCoords(4,7); return this; }
    public ChessBoardBuilder f8() { coords = new CellCoords(5,7); return this; }
    public ChessBoardBuilder g8() { coords = new CellCoords(6,7); return this; }
    public ChessBoardBuilder h8() { coords = new CellCoords(7,7); return this; }

}
