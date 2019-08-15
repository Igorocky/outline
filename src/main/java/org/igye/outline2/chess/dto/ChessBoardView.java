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
    private List<List<ChessBoardCellView>> cells;

    public void setBorderColor(CellCoords coords, String color) {
        getCell(coords).setBorderColor(color);
    }

    public ChessBoardCellView getCell(CellCoords coords) {
        return cells.get(coords.getX()).get(coords.getY());
    }
}
