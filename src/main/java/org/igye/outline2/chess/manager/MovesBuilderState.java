package org.igye.outline2.chess.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.Move;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovesBuilderState {
    private Move initialPosition;
    private Move currMove;
    private CellCoords preparedMoveFrom;
}
