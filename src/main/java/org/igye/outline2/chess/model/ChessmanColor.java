package org.igye.outline2.chess.model;

public enum ChessmanColor {
    WHITE, BLACK
    ;

    public ChessmanColor invert() {
        if (this.equals(WHITE)) {
            return BLACK;
        } else {
            return WHITE;
        }
    }
}
