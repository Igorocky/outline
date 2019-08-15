package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessBoardView;
import org.igye.outline2.chess.manager.ChessUtils;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanType;

import static org.igye.outline2.controllers.chess.ChessTestUtils.AVAILABLE_TO_MOVE_TO_COLOR;
import static org.igye.outline2.controllers.chess.ChessTestUtils.PREPARED_TO_MOVE_COLOR;

public class ChessBoardViewBuilder {
    private ChessBoardView chessBoard;

    public ChessBoardViewBuilder() {
        chessBoard = new ChessBoardView();
        chessBoard.setCells(ChessUtils.emptyBoard(8,8, (x, y)->{
            ChessBoardCellView cellDto = new ChessBoardCellView();
            cellDto.setCoords(new CellCoords(x,y));
            cellDto.setBorderColor(null);
            cellDto.setCode(0);
            return cellDto;
        }));
    }

    public ChessBoardView build() {
        return chessBoard;
    }

    public ChessBoardViewBuilder uP(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_PAWN.getCode()); }
    public ChessBoardViewBuilder yP(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_PAWN.getCode()); }
    public ChessBoardViewBuilder gP(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_PAWN.getCode()); }

    public ChessBoardViewBuilder uN(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_KNIGHT.getCode()); }
    public ChessBoardViewBuilder yN(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_KNIGHT.getCode()); }
    public ChessBoardViewBuilder gN(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_KNIGHT.getCode()); }

    public ChessBoardViewBuilder uB(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_BISHOP.getCode()); }
    public ChessBoardViewBuilder yB(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_BISHOP.getCode()); }
    public ChessBoardViewBuilder gB(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_BISHOP.getCode()); }

    public ChessBoardViewBuilder uR(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_ROOK.getCode()); }
    public ChessBoardViewBuilder yR(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_ROOK.getCode()); }
    public ChessBoardViewBuilder gR(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_ROOK.getCode()); }

    public ChessBoardViewBuilder uQ(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_QUEEN.getCode()); }
    public ChessBoardViewBuilder yQ(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_QUEEN.getCode()); }
    public ChessBoardViewBuilder gQ(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_QUEEN.getCode()); }

    public ChessBoardViewBuilder uK(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_KING.getCode()); }
    public ChessBoardViewBuilder yK(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_KING.getCode()); }
    public ChessBoardViewBuilder gK(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_KING.getCode()); }

    public ChessBoardViewBuilder up(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_PAWN.getCode()); }
    public ChessBoardViewBuilder yp(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_PAWN.getCode()); }
    public ChessBoardViewBuilder gp(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_PAWN.getCode()); }

    public ChessBoardViewBuilder un(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_KNIGHT.getCode()); }
    public ChessBoardViewBuilder yn(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_KNIGHT.getCode()); }
    public ChessBoardViewBuilder gn(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_KNIGHT.getCode()); }

    public ChessBoardViewBuilder ub(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_BISHOP.getCode()); }
    public ChessBoardViewBuilder yb(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_BISHOP.getCode()); }
    public ChessBoardViewBuilder gb(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_BISHOP.getCode()); }

    public ChessBoardViewBuilder ur(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_ROOK.getCode()); }
    public ChessBoardViewBuilder yr(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_ROOK.getCode()); }
    public ChessBoardViewBuilder gr(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_ROOK.getCode()); }

    public ChessBoardViewBuilder uq(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_QUEEN.getCode()); }
    public ChessBoardViewBuilder yq(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_QUEEN.getCode()); }
    public ChessBoardViewBuilder gq(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_QUEEN.getCode()); }

    public ChessBoardViewBuilder uk(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_KING.getCode()); }
    public ChessBoardViewBuilder yk(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_KING.getCode()); }
    public ChessBoardViewBuilder gk(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_KING.getCode()); }

    public ChessBoardViewBuilder uo(CellCoords coords) { return setCell(chessBoard, coords, null, 0); }
    public ChessBoardViewBuilder yo(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, 0); }
    public ChessBoardViewBuilder go(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, 0); }

    private ChessBoardViewBuilder setCell(ChessBoardView cbv, CellCoords coords, String borderColor, int code) {
        ChessBoardCellView cell = cbv.getCell(coords);
        cell.setBorderColor(borderColor);
        cell.setCode(code);
        return this;
    }
}
