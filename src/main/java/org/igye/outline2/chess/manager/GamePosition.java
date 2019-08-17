package org.igye.outline2.chess.manager;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.igye.outline2.chess.model.Move;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GamePosition {
    private GamePosition parent;
    private Move move;
    private List<GamePosition> children = new ArrayList<>();

    public GamePosition(Move move) {
        this.move = move;
    }

    public GamePosition(GamePosition parent, Move move) {
        this.parent = parent;
        this.move = move;
    }
}
