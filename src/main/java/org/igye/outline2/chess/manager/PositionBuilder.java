package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.InitialPositionView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;

import java.util.function.Consumer;

import static org.igye.outline2.common.OutlineUtils.nullSafeGetterWithDefault;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KING;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_ROOK;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KING;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_ROOK;

public class PositionBuilder implements ChessComponentStateManager {
    public static final int RECYCLE_BIN_CODE = 10007;
    private static final String SELECTED_CELL_BACKGROUND_COLOR = "yellow";
    private static final String NOT_SELECTED_CELL_BACKGROUND_COLOR = "white";

    private ChessBoard chessBoard;
    private ChessmanColor nextMoveColor = ChessmanColor.WHITE;

    private ChessBoardCellView[][] availablePieces;
    private int selectedCode;

    public PositionBuilder(String initialPosition) {
        chessBoard = new ChessBoard(initialPosition);
//        chessBoard.placePiece(new CellCoords(4,0), WHITE_KING);
//        chessBoard.placePiece(new CellCoords(4,7), BLACK_KING);
        initAvailablePieces();
        unhighlightAvailablePieces();
        availablePieces[6][1] = createCell(6,1, RECYCLE_BIN_CODE);

        ChessBoardCellView selectedPiece = availablePieces[0][1];
        selectedPiece.setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
        selectedCode = selectedPiece.getCode();
    }

    @Override
    public ChessComponentView toView() {
        ChessComponentView result = new ChessComponentView();
        result.setChessBoard(ChessViewConverter.toDto(chessBoard));
        result.setTab(ChessComponentStage.INITIAL_POSITION);
        result.setAvailableChessmanTypes(InitialPositionView.builder()
                .availableChessmanTypes(availablePieces)
                .nextMove(nextMoveColor)
                .build()
        );
        return result;
    }

    @Override
    public ChessComponentView cellLeftClicked(CellCoords coords) {
        if (coords.getX() >= 10) {
            ChessBoardCellView availablePiece = findAvailablePiece(coords);
            if (availablePiece.getCode() > 0) {
                unhighlightAvailablePieces();
                availablePiece.setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
                selectedCode = availablePiece.getCode();
            }
        } else if (selectedCode == RECYCLE_BIN_CODE) {
            chessBoard.placePiece(coords, null);
        } else {
            int codeOnTheCell = nullSafeGetterWithDefault(
                    chessBoard.getPieceAt(coords),
                    ChessmanType::getCode,
                    -1
            );
            if (selectedCode == codeOnTheCell) {
                chessBoard.placePiece(coords, null);
            } else {
                chessBoard.placePiece(coords, ChessmanType.fromCode(selectedCode));
            }
        }
        return toView();
    }

    @Override
    public ChessComponentView execChessCommand(String command) {
        notImplemented();
        return null;
    }

    public String getPosition() {
        return chessBoard.toFen();
    }

    public ChessmanColor getNextMoveColor() {
        return nextMoveColor;
    }

    private void initAvailablePieces() {
        ChessmanType[][] availableChessmen = new ChessmanType[][]{
                {WHITE_PAWN, BLACK_PAWN},
                {WHITE_KNIGHT, BLACK_KNIGHT},
                {WHITE_BISHOP, BLACK_BISHOP},
                {WHITE_ROOK, BLACK_ROOK},
                {WHITE_QUEEN, BLACK_QUEEN},
                {WHITE_KING, BLACK_KING},
                {null, null},
        };
        this.availablePieces = ChessUtils.emptyBoard(
                ChessBoardCellView[].class, ChessBoardCellView.class, 7, 2, (x,y)->
                createCell(x, y, availableChessmen[x][y])
        );
    }

    private void traverseCells(ChessBoardCellView[][] cells, Consumer<ChessBoardCellView> consumer) {
        for (ChessBoardCellView[] row : cells) {
            for (ChessBoardCellView cell : row) {
                consumer.accept(cell);
            }
        }
    }

    private ChessBoardCellView createCell(int x, int y, int code) {
        return ChessBoardCellView.builder()
                .backgroundColor(NOT_SELECTED_CELL_BACKGROUND_COLOR)
                .code(code)
                .coords(new CellCoords(x + 10, y))
                .build();
    }

    private ChessBoardCellView createCell(int x, int y, ChessmanType chessmanType) {
        return createCell(x, y, chessmanType ==null?0: chessmanType.getCode());
    }

    private void unhighlightAvailablePieces() {
        traverseCells(availablePieces, cell -> {
            if (cell!=null) {
                cell.setBackgroundColor(NOT_SELECTED_CELL_BACKGROUND_COLOR);
            }
        });
    }

    private ChessBoardCellView findAvailablePiece(CellCoords coords) {
        final ChessBoardCellView[] result = new ChessBoardCellView[1];
        traverseCells(availablePieces, cell -> {
            if (cell!=null && cell.getCoords().equals(coords)) {
                result[0] = cell;
            }
        });
        return result[0];
    }
}
