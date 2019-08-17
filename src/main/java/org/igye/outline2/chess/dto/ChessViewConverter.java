package org.igye.outline2.chess.dto;

import org.igye.outline2.chess.manager.ChessUtils;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanType;

public class ChessViewConverter {

    public static ChessBoardView toDto(ChessBoard chessBoard) {
        ChessBoardView chessBoardView = new ChessBoardView();
        chessBoardView.setCells(ChessUtils.emptyBoard(8,8, (x, y)->{
            ChessBoardCellView cellDto = new ChessBoardCellView();
            cellDto.setCoords(new CellCoords(x,y));
            ChessmanType chessman = chessBoard.getBoard().get(x).get(y);
            cellDto.setBackgroundColor((x + y) % 2 == 0 ? "#DCDCDC" : "white");
            cellDto.setCode(chessman != null ? chessman.getCode() : 0);
            return cellDto;
        }));
        return chessBoardView;
    }

}