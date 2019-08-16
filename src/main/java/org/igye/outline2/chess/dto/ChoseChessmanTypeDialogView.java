package org.igye.outline2.chess.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChoseChessmanTypeDialogView {
    private List<ChessBoardCellView> cellsToChoseFrom = new ArrayList<>();
}
