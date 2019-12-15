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
public class ChessComponentView {
    private ChessBoardView chessBoard;
    private ChessComponentStage tab;
    private InitialPositionView availableChessmanTypes;
    private HistoryView history;
    private String commandErrorMsg;
    private String commandResponseMsg;
    private PracticeStateView practiseState;
    private int pgnHashCode;
}
