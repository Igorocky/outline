package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveDto {
    private int feMoveNumber;

    private String whitesMove;
    private boolean whitesMoveSelected;

    private String blacksMove;
    private boolean blacksMoveSelected;
}
