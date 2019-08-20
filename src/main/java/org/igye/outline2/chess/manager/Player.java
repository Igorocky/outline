package org.igye.outline2.chess.manager;

import lombok.Getter;
import org.igye.outline2.chess.model.Move;

import java.util.List;

public class Player {
    @Getter
    private String name;

    public Move makeChoice(List<Move> availableMoves) {
        return null;
    }
}
