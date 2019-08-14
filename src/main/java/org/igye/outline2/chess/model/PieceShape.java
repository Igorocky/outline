package org.igye.outline2.chess.model;

import org.igye.outline2.exceptions.OutlineException;

import java.util.HashMap;
import java.util.Map;

public enum PieceShape {
    PAWN('p'),
    KNIGHT('n'),
    BISHOP('b'),
    ROOK('r'),
    QUEEN('q'),
    KING('k'),
    ;

    private Character symbol;

    private static Map<Character, PieceShape> symbolToValueMap = new HashMap<>();

    static {
        for (PieceShape value : values()) {
            symbolToValueMap.put(value.getSymbol(), value);
        }
    }

    PieceShape(Character symbol) {
        this.symbol = symbol;
    }

    public Character getSymbol() {
        return symbol;
    }

    public static PieceShape fromSymbol(Character symbol) {
        if (!symbolToValueMap.containsKey(symbol)) {
            throw new OutlineException("!symbolToValueMap.containsKey('" + symbol + "')");
        }
        return symbolToValueMap.get(symbol);
    }
}
