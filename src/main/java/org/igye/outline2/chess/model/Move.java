package org.igye.outline2.chess.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Move {
    private Move parentMove;
    private String name;
    private CellCoords from;
    private CellCoords to;
    private ChessBoard resultPosition;
    @Builder.Default
    private boolean whiteKingCastle = true;
    @Builder.Default
    private boolean whiteQueenCastle = true;
    @Builder.Default
    private boolean blackKingCastle = true;
    @Builder.Default
    private boolean blackQueenCastle = true;
    @Builder.Default
    private List<Move> nextMoves = new ArrayList<>();

    public ChessmanColor getColorOfWhoMadeMove() {
        return getResultPosition().getPieceAt(getTo()).getType().getPieceColor();
    }
}
