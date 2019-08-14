package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentDto;
import org.igye.outline2.chess.dto.ChessDtoConverter;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;

public class MovesBuilder implements ChessComponentStateManager {
    private static final String SELECTED_CELL_BACKGROUND_COLOR = "yellow";
    private static final String NOT_SELECTED_CELL_BACKGROUND_COLOR = "white";
    private ChessBoard chessBoard = new ChessBoard();


    public MovesBuilder() {
    }

    @Override
    public ChessComponentDto toDto() {
        ChessComponentDto result = new ChessComponentDto();
        result.setChessBoard(ChessDtoConverter.toDto(chessBoard));
        return result;
    }

    @Override
    public ChessComponentDto cellLeftClicked(CellCoords coords) {
        return toDto();
    }
}
