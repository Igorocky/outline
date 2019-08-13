package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessBoardCellDto;
import org.igye.outline2.chess.dto.ChessComponentDto;
import org.igye.outline2.chess.dto.ChessDtoConverter;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.Piece;

import java.util.List;
import java.util.function.Consumer;

import static org.igye.outline2.chess.model.Piece.BLACK_BISHOP;
import static org.igye.outline2.chess.model.Piece.BLACK_KING;
import static org.igye.outline2.chess.model.Piece.BLACK_KNIGHT;
import static org.igye.outline2.chess.model.Piece.BLACK_PAWN;
import static org.igye.outline2.chess.model.Piece.BLACK_QUEEN;
import static org.igye.outline2.chess.model.Piece.BLACK_ROOK;
import static org.igye.outline2.chess.model.Piece.WHITE_BISHOP;
import static org.igye.outline2.chess.model.Piece.WHITE_KING;
import static org.igye.outline2.chess.model.Piece.WHITE_KNIGHT;
import static org.igye.outline2.chess.model.Piece.WHITE_PAWN;
import static org.igye.outline2.chess.model.Piece.WHITE_QUEEN;
import static org.igye.outline2.chess.model.Piece.WHITE_ROOK;

public class PositionBuilder implements ChessComponentStateManager {
    public static final int RECYCLE_BIN_CODE = 128465;
    private ChessBoard chessBoard = new ChessBoard();

    private List<List<ChessBoardCellDto>> availablePieces;
    private int selectedCode;

    public PositionBuilder() {
        initAvailablePieces();
        unhighlightAvailablePieces();
        availablePieces.get(6).set(1, createCell(6,1, RECYCLE_BIN_CODE));

        ChessBoardCellDto selectedPiece = availablePieces.get(0).get(1);
        selectedPiece.setHighlighted(true);
        selectedCode = selectedPiece.getCode();
    }

    @Override
    public ChessComponentDto toDto() {
        ChessComponentDto result = new ChessComponentDto();
        result.setChessBoard(ChessDtoConverter.toDto(chessBoard));
        result.setAvailablePieces(availablePieces);
        return result;
    }

    @Override
    public ChessComponentDto cellLeftClicked(CellCoords coords) {
        if (coords.getX() >= 10) {
            unhighlightAvailablePieces();
            ChessBoardCellDto availablePiece = findAvailablePiece(coords);
            availablePiece.setHighlighted(true);
            selectedCode = availablePiece.getCode();
        } else {
            chessBoard.placePiece(coords, Piece.fromCode(selectedCode));
        }
        return toDto();
    }

    private void initAvailablePieces() {
        Piece[][] availablePieces = new Piece[][]{
                {WHITE_PAWN, BLACK_PAWN},
                {WHITE_KNIGHT, BLACK_KNIGHT},
                {WHITE_BISHOP, BLACK_BISHOP},
                {WHITE_ROOK, BLACK_ROOK},
                {WHITE_QUEEN, BLACK_QUEEN},
                {WHITE_KING, BLACK_KING},
                {null, null},
        };
        this.availablePieces = ChessUtils.emptyBoard(7, 2, (x,y)->
                createCell(x, y, availablePieces[x][y])
        );
    }

    private ChessBoardCellDto findCellByCoords(List<List<ChessBoardCellDto>> cells, CellCoords coords) {
        final ChessBoardCellDto[] result = {null};
        traverseCells(cells, cell -> {
            if (cell!=null) {
                if (cell.getCoords().equals(coords)) {
                    result[0] = cell;
                }
            }
        });
        return result[0];
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
                .backgroundColor("white")
                .code(code)
                .coords(new CellCoords(x + 10, y))
                .build();
    }

    private ChessBoardCellDto createCell(int x, int y, Piece piece) {
        return createCell(x, y, piece==null?0:piece.getCode());
    }

    private void unhighlightAvailablePieces() {
        traverseCells(availablePieces, cell -> {
            if (cell!=null) {
                cell.setHighlighted(false);
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
