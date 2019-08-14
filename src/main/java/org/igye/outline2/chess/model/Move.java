package org.igye.outline2.chess.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Move {
    private ChessmanColor color;
    private CellCoords from;
    private CellCoords to;
    private ChessBoard resultPosition;
}
