package org.igye.outline2.chess.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CellCoords {
    private int x;
    private int y;

    public CellCoords plusX(int dx) {
        return CellCoords.builder().x(x+dx).y(y).build();
    }

    public CellCoords plusY(int dy) {
        return CellCoords.builder().x(x).y(y+dy).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellCoords that = (CellCoords) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
