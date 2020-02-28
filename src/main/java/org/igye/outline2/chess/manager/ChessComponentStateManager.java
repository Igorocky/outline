package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentResponse;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.exceptions.OutlineException;

import java.util.function.Consumer;

public interface ChessComponentStateManager {
    ChessComponentResponse toView();
    ChessComponentResponse cellLeftClicked(CellCoords coords);
    ChessComponentResponse execChessCommand(String command, Consumer<String> progressCallback);
    ChessComponentResponse setColorToMove(ChessmanColor colorToMove);
    ChessComponentResponse changeCastlingAvailability(ChessmanColor color, boolean isLong);
    ChessComponentResponse setPositionFromFen(String fen);
    ChessComponentResponse showCorrectMove();
    ChessComponentResponse setAutoResponseForOpponent();
    ChessComponentResponse hideCommandResponseMsg();

    default void notSupported() {
        throw new OutlineException("Method not supported.");
    }

    default void notImplemented() {
        throw new OutlineException("Method not implemented.");
    }
}
