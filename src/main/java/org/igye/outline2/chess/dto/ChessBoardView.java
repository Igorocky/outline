package org.igye.outline2.chess.dto;

import lombok.*;
import org.igye.outline2.chess.model.CellCoords;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChessBoardView {
    private ChessBoardCellView[][] cells;
    private boolean boardRotated;

    public void setBorderColorForCell(CellCoords coords, String color) {
        getCell(coords).setBorderColor(color);
    }

    public ChessBoardCellView getCell(CellCoords coords) {
        return getCell(coords.getX(), coords.getY());
    }

    public ChessBoardCellView getCell(int x, int y) {
        return cells[x][y];
    }
}
