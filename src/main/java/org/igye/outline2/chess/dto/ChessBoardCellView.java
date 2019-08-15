package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.chess.model.CellCoords;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoardCellView that = (ChessBoardCellView) o;
        return code == that.code &&
                Objects.equals(coords, that.coords) &&
                Objects.equals(backgroundColor, that.backgroundColor) &&
                Objects.equals(borderColor, that.borderColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coords, backgroundColor, borderColor, code);
    }
}
