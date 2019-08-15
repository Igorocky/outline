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
    private boolean whiteKingCastleAvailable = true;
    @Builder.Default
    private boolean whiteQueenCastleAvailable = true;
    @Builder.Default
    private boolean blackKingCastleAvailable = true;
    @Builder.Default
    private boolean blackQueenCastleAvailable = true;
    @Builder.Default
    private List<Move> nextMoves = new ArrayList<>();

    public ChessmanColor getColorOfWhoMadeMove() {
        return getResultPosition().getPieceAt(getTo()).getType().getPieceColor();
    }
}
