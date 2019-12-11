package org.igye.outline2.chess.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeStateView {
    private boolean waitingForNextMove;
    private boolean incorrectMove;
    private boolean failed;
}
