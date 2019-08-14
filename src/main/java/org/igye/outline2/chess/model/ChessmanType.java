package org.igye.outline2.chess.model;

import lombok.Getter;
import org.igye.outline2.exceptions.OutlineException;

import static org.igye.outline2.chess.model.ChessmanColor.BLACK;
import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.chess.model.PieceShape.BISHOP;
import static org.igye.outline2.chess.model.PieceShape.KING;
import static org.igye.outline2.chess.model.PieceShape.KNIGHT;
import static org.igye.outline2.chess.model.PieceShape.PAWN;
import static org.igye.outline2.chess.model.PieceShape.QUEEN;
import static org.igye.outline2.chess.model.PieceShape.ROOK;

@Getter
public enum ChessmanType {
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
    private ChessmanColor pieceColor;
    private PieceShape pieceShape;
    private int code;

    ChessmanType(ChessmanColor pieceColor, PieceShape pieceShape, int code) {
        this.pieceColor = pieceColor;
        this.pieceShape = pieceShape;
        this.code = code;
    }

    public static ChessmanType fromCode(int code) {
        for (ChessmanType chessmanType : values()) {
            if (chessmanType.getCode()==code) {
                return chessmanType;
            }
        }
        throw new OutlineException("Cannot find any chessman with code " + code + ".");
    }

    public static ChessmanType fromSymbol(Character symbol) {
        Character lowerCaseSymbol = symbol.toString().toLowerCase().charAt(0);
        PieceShape shape = PieceShape.fromSymbol(lowerCaseSymbol);
        ChessmanColor color = symbol.equals(lowerCaseSymbol) ? BLACK : WHITE;
        return getByColorAndShape(color, shape);
    }

    private static ChessmanType getByColorAndShape(ChessmanColor color, PieceShape shape) {
        for (ChessmanType value : values()) {
            if (value.getPieceColor().equals(color) && value.getPieceShape().equals(shape)) {
                return value;
            }
        }
        throw new OutlineException("getByColorAndShape");
    }
}