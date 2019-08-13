package org.igye.outline2.chess.dto;

import org.igye.outline2.chess.manager.ChessUtils;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.Piece;

public class ChessDtoConverter {

    public static ChessBoardDto toDto(ChessBoard chessBoard) {
        ChessBoardDto chessBoardDto = new ChessBoardDto();
        chessBoardDto.setCells(ChessUtils.emptyBoard(8,8, (x,y)->{
            ChessBoardCellDto cellDto = new ChessBoardCellDto();
            cellDto.setCoords(new CellCoords(x,y));
            Piece piece = chessBoard.getBoard().get(x).get(y);
            cellDto.setBackgroundColor((x + y) % 2 == 0 ? "lightseagreen" : "white");
            cellDto.setCode(piece != null ? piece.getCode() : 0);
            return cellDto;
        }));
        return chessBoardDto;
    }

}