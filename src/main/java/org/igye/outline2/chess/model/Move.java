package org.igye.outline2.chess.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Move {
    private CellCoords from;
    private CellCoords to;
}
