package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.chess.model.ChessmanColor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitialPositionView {
    private ChessBoardCellView[][] availableChessmanTypes;
    private ChessmanColor nextMove;
}
