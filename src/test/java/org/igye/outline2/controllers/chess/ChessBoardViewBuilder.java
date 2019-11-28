package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessBoardView;
import org.igye.outline2.chess.manager.ChessUtils;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanType;

import static org.igye.outline2.controllers.chess.ChessTestUtils.AVAILABLE_TO_MOVE_TO_COLOR;
import static org.igye.outline2.controllers.chess.ChessTestUtils.CHOOSE_CHESSMAN_TYPE_COLOR;
import static org.igye.outline2.controllers.chess.ChessTestUtils.PREPARED_TO_MOVE_COLOR;

public class ChessBoardViewBuilder {
    private ChessBoardView chessBoard;

    public ChessBoardViewBuilder() {
        chessBoard = new ChessBoardView();
        chessBoard.setCells(ChessUtils.emptyBoard(
                ChessBoardCellView[].class, ChessBoardCellView.class, 8,8, (x, y)->{
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

    public ChessBoardViewBuilder _P(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_PAWN.getCode()); }
    public ChessBoardViewBuilder yP(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_PAWN.getCode()); }
    public ChessBoardViewBuilder gP(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_PAWN.getCode()); }
    public ChessBoardViewBuilder bP(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.WHITE_PAWN.getCode()); }

    public ChessBoardViewBuilder _N(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_KNIGHT.getCode()); }
    public ChessBoardViewBuilder yN(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_KNIGHT.getCode()); }
    public ChessBoardViewBuilder gN(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_KNIGHT.getCode()); }
    public ChessBoardViewBuilder bN(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.WHITE_KNIGHT.getCode()); }

    public ChessBoardViewBuilder _B(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_BISHOP.getCode()); }
    public ChessBoardViewBuilder yB(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_BISHOP.getCode()); }
    public ChessBoardViewBuilder gB(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_BISHOP.getCode()); }
    public ChessBoardViewBuilder bB(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.WHITE_BISHOP.getCode()); }

    public ChessBoardViewBuilder _R(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_ROOK.getCode()); }
    public ChessBoardViewBuilder yR(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_ROOK.getCode()); }
    public ChessBoardViewBuilder gR(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_ROOK.getCode()); }
    public ChessBoardViewBuilder bR(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.WHITE_ROOK.getCode()); }

    public ChessBoardViewBuilder _Q(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_QUEEN.getCode()); }
    public ChessBoardViewBuilder yQ(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_QUEEN.getCode()); }
    public ChessBoardViewBuilder gQ(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_QUEEN.getCode()); }
    public ChessBoardViewBuilder bQ(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.WHITE_QUEEN.getCode()); }

    public ChessBoardViewBuilder _K(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.WHITE_KING.getCode()); }
    public ChessBoardViewBuilder yK(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.WHITE_KING.getCode()); }
    public ChessBoardViewBuilder gK(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.WHITE_KING.getCode()); }
    public ChessBoardViewBuilder bK(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.WHITE_KING.getCode()); }

    public ChessBoardViewBuilder _p(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_PAWN.getCode()); }
    public ChessBoardViewBuilder yp(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_PAWN.getCode()); }
    public ChessBoardViewBuilder gp(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_PAWN.getCode()); }
    public ChessBoardViewBuilder bp(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.BLACK_PAWN.getCode()); }

    public ChessBoardViewBuilder _n(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_KNIGHT.getCode()); }
    public ChessBoardViewBuilder yn(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_KNIGHT.getCode()); }
    public ChessBoardViewBuilder gn(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_KNIGHT.getCode()); }
    public ChessBoardViewBuilder bn(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.BLACK_KNIGHT.getCode()); }

    public ChessBoardViewBuilder _b(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_BISHOP.getCode()); }
    public ChessBoardViewBuilder yb(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_BISHOP.getCode()); }
    public ChessBoardViewBuilder gb(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_BISHOP.getCode()); }
    public ChessBoardViewBuilder bb(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.BLACK_BISHOP.getCode()); }

    public ChessBoardViewBuilder _r(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_ROOK.getCode()); }
    public ChessBoardViewBuilder yr(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_ROOK.getCode()); }
    public ChessBoardViewBuilder gr(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_ROOK.getCode()); }
    public ChessBoardViewBuilder br(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.BLACK_ROOK.getCode()); }

    public ChessBoardViewBuilder _q(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_QUEEN.getCode()); }
    public ChessBoardViewBuilder yq(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_QUEEN.getCode()); }
    public ChessBoardViewBuilder gq(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_QUEEN.getCode()); }
    public ChessBoardViewBuilder bq(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.BLACK_QUEEN.getCode()); }

    public ChessBoardViewBuilder _k(CellCoords coords) { return setCell(chessBoard, coords, null, ChessmanType.BLACK_KING.getCode()); }
    public ChessBoardViewBuilder yk(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, ChessmanType.BLACK_KING.getCode()); }
    public ChessBoardViewBuilder gk(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, ChessmanType.BLACK_KING.getCode()); }
    public ChessBoardViewBuilder bk(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, ChessmanType.BLACK_KING.getCode()); }

    public ChessBoardViewBuilder __(CellCoords coords) { return setCell(chessBoard, coords, null, 0); }
    public ChessBoardViewBuilder y_(CellCoords coords) { return setCell(chessBoard, coords, PREPARED_TO_MOVE_COLOR, 0); }
    public ChessBoardViewBuilder g_(CellCoords coords) { return setCell(chessBoard, coords, AVAILABLE_TO_MOVE_TO_COLOR, 0); }
    public ChessBoardViewBuilder b_(CellCoords coords) { return setCell(chessBoard, coords, CHOOSE_CHESSMAN_TYPE_COLOR, 0); }

    private ChessBoardViewBuilder setCell(ChessBoardView cbv, CellCoords coords, String borderColor, int code) {
        ChessBoardCellView cell = cbv.getCell(coords);
        cell.setBorderColor(borderColor);
        cell.setCode(code);
        return this;
    }
}
