package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentDto;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.exceptions.OutlineException;

public interface ChessComponentStateManager {
    ChessComponentDto toDto();
    ChessComponentDto cellLeftClicked(CellCoords coords);

    default void notSupported() {
        throw new OutlineException("Method not supported.");
    }

    default void notImplemented() {
        throw new OutlineException("Method not implemented.");
    }
}
