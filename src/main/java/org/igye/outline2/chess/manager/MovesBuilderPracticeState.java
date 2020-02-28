package org.igye.outline2.chess.manager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovesBuilderPracticeState {
    private String lastMoveWasIncorrect;
    private boolean failed;
}
