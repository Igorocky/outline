package org.igye.outline2.chess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.igye.outline2.chess.manager.ChessUtils;

import java.util.Objects;

@Getter
public final class CellCoords {
    private final int x;
    private final int y;

    @JsonCreator
    public CellCoords(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    public CellCoords plusX(int dx) {
        return new CellCoords(x+dx, y);
    }

    public CellCoords plusY(int dy) {
        return new CellCoords(x, y+dy);
    }

    public CellCoords plusXY(int dx, int dy) {
        return new CellCoords(x+dx, y+dy);
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
