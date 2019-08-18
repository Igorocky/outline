package org.igye.outline2.chess.model;

import org.igye.outline2.exceptions.OutlineException;

import java.util.HashMap;
import java.util.Map;

public enum PieceShape {
    PAWN("P"),
    KNIGHT("N"),
    BISHOP("B"),
    ROOK("R"),
    QUEEN("Q"),
    KING("K"),
    ;

    private String symbol;

    private static Map<String, PieceShape> symbolToValueMap = new HashMap<>();

    static {
        for (PieceShape value : values()) {
            symbolToValueMap.put(value.getSymbol(), value);
        }
    }

    PieceShape(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static PieceShape fromSymbol(String symbol) {
        if (!symbolToValueMap.containsKey(symbol)) {
            throw new OutlineException("!symbolToValueMap.containsKey('" + symbol + "')");
        }
        return symbolToValueMap.get(symbol);
    }
}
