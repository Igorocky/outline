package org.igye.outline2.chess.model;

import static org.igye.outline2.chess.model.PieceColor.WHITE;
import static org.igye.outline2.chess.model.PieceShape.KNIGHT;
import static org.igye.outline2.chess.model.PieceShape.PAWN;

public enum Piece {
    WHITE_PAWN(WHITE, PAWN, 9817),
    WHITE_KNIGHT(WHITE, KNIGHT, 9816),
    ;
    private PieceColor pieceColor;
    private PieceShape pieceShape;
    private int code;

    Piece(PieceColor pieceColor, PieceShape pieceShape, int code) {
        this.pieceColor = pieceColor;
        this.pieceShape = pieceShape;
        this.code = code;
    }
}
