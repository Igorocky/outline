package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.chess.model.CellCoords;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChessBoardCellView {
    private CellCoords coords;
    private String backgroundColor;
    private String borderColor;
    private int code;
}
