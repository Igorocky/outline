package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.ChoseChessmanTypeDialogView;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.MoveView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.PieceShape;
import org.igye.outline2.exceptions.OutlineException;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.chess.manager.ChessUtils.moveToString;

public class MovesBuilder implements ChessComponentStateManager {
    public static final String PREPARED_TO_MOVE_COLOR = "#FFFF00";
    public static final String AVAILABLE_TO_MOVE_TO_COLOR = "#90EE90";

    private MovesBuilderState state;

    public MovesBuilder(Move initialPosition) {
        state = new MovesBuilderState(initialPosition);
    }

    @Override
    public ChessComponentView toView() {
        ChessComponentView chessComponentView = new ChessComponentView();
        chessComponentView.setChessBoard(ChessViewConverter.toDto(getCurrentPosition()));
        chessComponentView.setTab(ChessComponentStage.MOVES);
        final Move currMove = state.getCurrPosition().getMove();
        if (!CollectionUtils.isEmpty(state.getPreparedMoves())) {
            chessComponentView.getChessBoard().setBorderColorForCell(
                    state.getPreparedMoves().get(0).getFrom(), PREPARED_TO_MOVE_COLOR
            );
            state.getPreparedMoves().forEach(move ->
                    chessComponentView.getChessBoard().setBorderColorForCell(move.getTo(), AVAILABLE_TO_MOVE_TO_COLOR)
            );
        } else {
            if (currMove.getFrom() != null) {
                chessComponentView.getChessBoard().setBorderColorForCell(currMove.getFrom(), PREPARED_TO_MOVE_COLOR);
            }
            if (currMove.getTo() != null) {
                chessComponentView.getChessBoard().setBorderColorForCell(currMove.getTo(), AVAILABLE_TO_MOVE_TO_COLOR);
            }
        }
        chessComponentView.setHistory(toDto(state.getInitialPosition(), state.getCurrPosition()));
        createChoseChessmanTypeDialogViewIfNecessary(chessComponentView);
        return chessComponentView;
    }

    private void createChoseChessmanTypeDialogViewIfNecessary(ChessComponentView chessComponentView) {
        if (state.isChoseChessmanTypeDialogOpened()) {
            chessComponentView.setChoseChessmanTypeDialogView(
                    createChoseChessmanTypeDialogView(state.getPreparedMoves().get(0).getColorOfWhoMadeMove())
            );
        }
    }

    @Override
    public ChessComponentView cellLeftClicked(CellCoords coordsClicked) {
        if (state.isChoseChessmanTypeDialogOpened()) {
            processPawnOnLastLine(coordsClicked);
        } else if (state.getCurrPosition().getChildren().isEmpty()) {
            final List<Move> preparedMoves = state.getPreparedMoves();
            if (CollectionUtils.isEmpty(preparedMoves)) {
                List<Move> possibleMoves = state.getCurrPosition().getMove().getPossibleNextMoves(coordsClicked);
                if (!possibleMoves.isEmpty()) {
                    state.setPreparedMoves(possibleMoves);
                }
            } else if (coordsClicked.equals(preparedMoves.get(0).getFrom())) {
                state.setPreparedMoves(null);
            } else {
                List<Move> movesChosen = state.getPreparedMoves().stream()
                        .filter(move -> move.getTo().equals(coordsClicked))
                        .collect(Collectors.toList());
                if (!movesChosen.isEmpty()) {
                    state.setPreparedMoves(movesChosen);
                    if (movesChosen.size() > 1) {
                        state.setChoseChessmanTypeDialogOpened(true);
                    } else {
                        state.appendPreparedMoveToHistory();
                    }
                } else {
                    state.setPreparedMoves(null);
                    cellLeftClicked(coordsClicked);
                }
            }
        }
        return toView();
    }

    @Override
    public ChessComponentView execCommand(String command) {
        notImplemented();
        return null;
    }

    private void processPawnOnLastLine(CellCoords coordsClicked) {
        final int xCoord = coordsClicked.getX();
        if (20 <= xCoord && xCoord <= 23) {
            final PieceShape replacementShape =
                    xCoord == 20 ? PieceShape.KNIGHT :
                    xCoord == 21 ? PieceShape.BISHOP :
                    xCoord == 22 ? PieceShape.ROOK :
                    xCoord == 23 ? PieceShape.QUEEN : null;
            if (replacementShape == null) {
                throw new OutlineException("replacementShape == null");
            }
            state.setPreparedMoves(listOf(findPreparedMoveByPawnReplacementType(replacementShape)));
            state.appendPreparedMoveToHistory();
            state.setChoseChessmanTypeDialogOpened(false);
        }
    }

    private Move findPreparedMoveByPawnReplacementType(PieceShape pieceShape) {
        return state.getPreparedMoves().stream()
                .filter(move -> move.getPieceAt(move.getTo()).getPieceShape() == pieceShape)
                .findFirst()
                .get();
    }

    private ChoseChessmanTypeDialogView createChoseChessmanTypeDialogView(ChessmanColor color) {
        ChoseChessmanTypeDialogView result = new ChoseChessmanTypeDialogView();
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_KNIGHT.getCode()
                                : ChessmanType.BLACK_KNIGHT.getCode()
                        )
                        .coords(new CellCoords(20,0))
                        .build()
        );
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_BISHOP.getCode()
                                : ChessmanType.BLACK_BISHOP.getCode()
                        )
                        .coords(new CellCoords(21,0))
                        .build()
        );
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_ROOK.getCode()
                                : ChessmanType.BLACK_ROOK.getCode()
                        )
                        .coords(new CellCoords(22,0))
                        .build()
        );
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_QUEEN.getCode()
                                : ChessmanType.BLACK_QUEEN.getCode()
                        )
                        .coords(new CellCoords(23,0))
                        .build()
        );
        return result;
    }

    public String getInitialPosition() {
        return state.getInitialPosition().getMove().getResultPosition().encode();
    }

    private ChessBoard getCurrentPosition() {
        return state.getCurrPosition().getMove().getResultPosition();
    }

    private HistoryView toDto(GamePosition initialPosition, final GamePosition selectedPosition) {
        HistoryView historyView = new HistoryView();
        int feMoveNumber = 1;
        MoveView moveView = new MoveView();
        moveView.setFeMoveNumber(feMoveNumber);
        GamePosition currPosition = getNextOnlyPosition(initialPosition);
        while (currPosition != null) {
            final Move currMove = currPosition.getMove();
            if (currMove.getColorOfWhoMadeMove() == ChessmanColor.WHITE) {
                moveView.setWhitesMove(moveToString(currMove));
                moveView.setWhitesMoveSelected(selectedPosition == currPosition);
            } else {
                moveView.setBlacksMove(moveToString(currMove));
                moveView.setBlacksMoveSelected(selectedPosition == currPosition);

                historyView.getMoves().add(moveView);
                feMoveNumber++;
                moveView = new MoveView();
                moveView.setFeMoveNumber(feMoveNumber);
            }
            currPosition = getNextOnlyPosition(currPosition);
        }
        if (moveView.getWhitesMove() != null) {
            historyView.getMoves().add(moveView);
        }
        return historyView;
    }

    private GamePosition getNextOnlyPosition(GamePosition position) {
        List<GamePosition> nextMoves = position.getChildren();
        if (!nextMoves.isEmpty()) {
            if (nextMoves.size() == 1) {
                return nextMoves.get(0);
            } else {
                throw new OutlineException("Exception in getNextOnlyMove()");
            }
        } else {
            return null;
        }
    }
}
