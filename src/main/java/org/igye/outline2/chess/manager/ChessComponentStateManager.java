package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.exceptions.OutlineException;

public interface ChessComponentStateManager {
    ChessComponentView toView();
    ChessComponentView cellLeftClicked(CellCoords coords);
    ChessComponentView execChessCommand(String command);
    ChessComponentView setColorToMove(ChessmanColor colorToMove);
    ChessComponentView changeCastlingAvailability(ChessmanColor color, boolean isLong);

    default void notSupported() {
        throw new OutlineException("Method not supported.");
    }

    default void notImplemented() {
        throw new OutlineException("Method not implemented.");
    }
}
