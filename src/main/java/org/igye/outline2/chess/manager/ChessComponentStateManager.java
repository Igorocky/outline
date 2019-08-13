package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentDto;
import org.igye.outline2.chess.model.CellCoords;

public interface ChessComponentStateManager {
    ChessComponentDto toDto();
    ChessComponentDto cellLeftClicked(CellCoords coords);
}
