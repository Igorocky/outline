package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.chess.manager.ChessComponentStage;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChessComponentDto {
    private ChessBoardDto chessBoard;
    private ChessComponentStage tab;
    private InitialPositionDto availableChessmanTypes;
    private HistoryDto history;

}
