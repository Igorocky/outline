package org.igye.outline2.chess.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveDto {
    private String notation;
    private String board;
    private int cellFrom;
    private int cellTo;
}
