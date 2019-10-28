package org.igye.outline2.chess.dto;

import org.igye.outline2.chess.manager.ChessUtils;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanType;

public class ChessViewConverter {

    public static ChessBoardView toDto(ChessBoard chessBoard) {
        ChessBoardView chessBoardView = new ChessBoardView();
        chessBoardView.setCells(ChessUtils.emptyBoard(
                ChessBoardCellView[].class, ChessBoardCellView.class, 8,8, (x, y)->{
            ChessBoardCellView cellDto = new ChessBoardCellView();
            cellDto.setCoords(new CellCoords(x,y));
            ChessmanType chessman = chessBoard.getPieceAt(x, y);
            cellDto.setBackgroundColor((x + y) % 2 == 0 ? "rgb(181,136,99)" : "rgb(240,217,181)");
            cellDto.setCode(chessman != null ? chessman.getCode() : 0);
            return cellDto;
        }));
        return chessBoardView;
    }

}