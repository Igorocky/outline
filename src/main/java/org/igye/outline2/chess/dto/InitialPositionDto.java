package org.igye.outline2.chess.dto;

import lombok.*;
import org.igye.outline2.chess.model.ChessmanColor;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitialPositionDto {
    private List<List<ChessBoardCellDto>> availableChessmanTypes;
    private ChessmanColor nextMove;
}
