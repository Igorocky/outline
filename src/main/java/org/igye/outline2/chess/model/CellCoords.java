package org.igye.outline2.chess.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.igye.outline2.chess.manager.ChessUtils;

import java.util.Objects;

@Getter
@AllArgsConstructor
public final class CellCoords {
    private final int x;
    private final int y;

    public CellCoords plusX(int dx) {
        return new CellCoords(x+dx, y);
    }

    public CellCoords plusY(int dy) {
        return new CellCoords(x, y+dy);
    }

    @Override
    public String toString() {
        return ChessUtils.coordsToString(this);
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
