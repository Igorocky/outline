package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.chess.model.CellCoords;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChessBoardView {
    private ChessBoardCellView[][] cells;

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
