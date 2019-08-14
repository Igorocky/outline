package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.AvailableChessmanTypesDto;
import org.igye.outline2.chess.dto.ChessBoardCellDto;
import org.igye.outline2.chess.dto.ChessComponentDto;
import org.igye.outline2.chess.dto.ChessDtoConverter;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanType;

import java.util.List;
import java.util.function.Consumer;

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
    private ChessBoard chessBoard = new ChessBoard();

    private List<List<ChessBoardCellDto>> availablePieces;
    private int selectedCode;

    public PositionBuilder() {
        initAvailablePieces();
        unhighlightAvailablePieces();
        availablePieces.get(6).set(0, createCell(6,0, RECYCLE_BIN_CODE));

        ChessBoardCellDto selectedPiece = availablePieces.get(0).get(1);
        selectedPiece.setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
        selectedCode = selectedPiece.getCode();
    }

    @Override
    public ChessComponentDto toDto() {
        ChessComponentDto result = new ChessComponentDto();
        result.setChessBoard(ChessDtoConverter.toDto(chessBoard));
        result.setAvailableChessmanTypes(AvailableChessmanTypesDto.builder().availableChessmanTypes(availablePieces).build());
        return result;
    }

    @Override
    public ChessComponentDto cellLeftClicked(CellCoords coords) {
        if (coords.getX() >= 10) {
            ChessBoardCellDto availablePiece = findAvailablePiece(coords);
            if (availablePiece.getCode() > 0) {
                unhighlightAvailablePieces();
                availablePiece.setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
                selectedCode = availablePiece.getCode();
            }
        } else if (selectedCode == RECYCLE_BIN_CODE) {
            chessBoard.placePiece(coords, null);
        } else {
            chessBoard.placePiece(coords, ChessmanType.fromCode(selectedCode));
        }
        return toDto();
    }

    private void initAvailablePieces() {
        ChessmanType[][] availableChessmen = new ChessmanType[][]{
                {BLACK_PAWN, WHITE_PAWN},
                {BLACK_KNIGHT, WHITE_KNIGHT},
                {BLACK_BISHOP, WHITE_BISHOP},
                {BLACK_ROOK, WHITE_ROOK},
                {BLACK_QUEEN, WHITE_QUEEN},
                {BLACK_KING, WHITE_KING},
                {null, null},
        };
        this.availablePieces = ChessUtils.emptyBoard(7, 2, (x,y)->
                createCell(x, y, availableChessmen[x][y])
        );
    }

    private void traverseCells(List<List<ChessBoardCellDto>> cells, Consumer<ChessBoardCellDto> consumer) {
        for (List<ChessBoardCellDto> row : cells) {
            for (ChessBoardCellDto cell : row) {
                consumer.accept(cell);
            }
        }
    }

    private ChessBoardCellDto createCell(int x, int y, int code) {
        return ChessBoardCellDto.builder()
                .backgroundColor(NOT_SELECTED_CELL_BACKGROUND_COLOR)
                .code(code)
                .coords(new CellCoords(x + 10, y))
                .build();
    }

    private ChessBoardCellDto createCell(int x, int y, ChessmanType chessmanType) {
        return createCell(x, y, chessmanType ==null?0: chessmanType.getCode());
    }

    private void unhighlightAvailablePieces() {
        traverseCells(availablePieces, cell -> {
            if (cell!=null) {
                cell.setBackgroundColor(NOT_SELECTED_CELL_BACKGROUND_COLOR);
            }
        });
    }

    private ChessBoardCellDto findAvailablePiece(CellCoords coords) {
        final ChessBoardCellDto[] result = new ChessBoardCellDto[1];
        traverseCells(availablePieces, cell -> {
            if (cell!=null && cell.getCoords().equals(coords)) {
                result[0] = cell;
            }
        });
        return result[0];
    }
}
