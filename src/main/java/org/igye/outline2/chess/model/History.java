package org.igye.outline2.chess.model;

import java.util.ArrayList;
import java.util.List;

public class History {
    private List<Move> moves;

    public History(ChessmanColor lastMovedColor, ChessBoard initialPosition) {
        moves = new ArrayList<>();
        moves.add(Move.builder().color(lastMovedColor).resultPosition(initialPosition).build());
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }
}
