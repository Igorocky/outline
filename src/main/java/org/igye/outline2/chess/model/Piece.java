package org.igye.outline2.chess.model;

import lombok.Getter;
import org.igye.outline2.exceptions.OutlineException;

import static org.igye.outline2.chess.model.PieceColor.BLACK;
import static org.igye.outline2.chess.model.PieceColor.WHITE;
import static org.igye.outline2.chess.model.PieceShape.BISHOP;
import static org.igye.outline2.chess.model.PieceShape.KING;
import static org.igye.outline2.chess.model.PieceShape.KNIGHT;
import static org.igye.outline2.chess.model.PieceShape.PAWN;
import static org.igye.outline2.chess.model.PieceShape.QUEEN;
import static org.igye.outline2.chess.model.PieceShape.ROOK;

@Getter
public enum Piece {
    WHITE_PAWN(WHITE, PAWN, 9817),
    WHITE_KNIGHT(WHITE, KNIGHT, 9816),
    WHITE_BISHOP(WHITE, BISHOP, 9815),
    WHITE_ROOK(WHITE, ROOK, 9814),
    WHITE_QUEEN(WHITE, QUEEN, 9813),
    WHITE_KING(WHITE, KING, 9812),
    BLACK_PAWN(BLACK, PAWN, 9823),
    BLACK_KNIGHT(BLACK, KNIGHT, 9822),
    BLACK_BISHOP(BLACK, BISHOP, 9821),
    BLACK_ROOK(BLACK, ROOK, 9820),
    BLACK_QUEEN(BLACK, QUEEN, 9819),
    BLACK_KING(BLACK, KING, 9818),
    ;
    private PieceColor pieceColor;
    private PieceShape pieceShape;
    private int code;

    Piece(PieceColor pieceColor, PieceShape pieceShape, int code) {
        this.pieceColor = pieceColor;
        this.pieceShape = pieceShape;
        this.code = code;
    }

    public static Piece fromCode(int code) {
        for (Piece piece : values()) {
            if (piece.getCode()==code) {
                return piece;
            }
        }
        throw new OutlineException("Cannot find any piece with code " + code + ".");
    }
}
