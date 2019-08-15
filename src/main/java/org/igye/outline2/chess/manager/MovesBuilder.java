package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.MoveView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.Chessman;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.exceptions.OutlineException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.igye.outline2.chess.manager.ChessUtils.moveToString;

public class MovesBuilder implements ChessComponentStateManager {
    private MovesBuilderState state;

    public MovesBuilder(Move initialPosition) {
        state = MovesBuilderState.builder()
                .initialPosition(initialPosition)
                .currMove(initialPosition)
                .build();
    }

    @Override
    public ChessComponentView toView() {
        ChessComponentView result = new ChessComponentView();
        result.setChessBoard(ChessViewConverter.toDto(getCurrentPosition()));
        result.setTab(ChessComponentStage.MOVES);
        if (state.getPreparedMoveFrom() != null) {
            result.getChessBoard().setBorderColor(state.getPreparedMoveFrom(), "yellow");
            getPossibleMoves(state.getPreparedMoveFrom()).forEach(coords ->
                    result.getChessBoard().setBorderColor(coords, "green")
            );
        }
        result.setHistory(toDto(state.getInitialPosition(), state.getCurrMove()));
        return result;
    }

    @Override
    public ChessComponentView cellLeftClicked(CellCoords coords) {
        final CellCoords preparedMoveFrom = state.getPreparedMoveFrom();
        if (preparedMoveFrom == null) {
            Set<CellCoords> possibleMoves = getPossibleMoves(coords);
            if (!possibleMoves.isEmpty()) {
                state.setPreparedMoveFrom(coords);
            }
        } else if (coords.equals(preparedMoveFrom)) {
            state.setPreparedMoveFrom(null);
        } else {
            Set<CellCoords> possibleMoves = getPossibleMoves(preparedMoveFrom);
            if (possibleMoves.contains(coords)) {
                final Move currMove = state.getCurrMove();
                Move newMove = Move.builder()
                        .from(preparedMoveFrom)
                        .to(coords)
                        .resultPosition(makeMove(currMove.getResultPosition().encode(), preparedMoveFrom, coords))
                        .build();
                currMove.getNextMoves().add(newMove);
                state.setCurrMove(newMove);
                state.setPreparedMoveFrom(null);
            } else {
                state.setPreparedMoveFrom(null);
            }
        }
        return toView();
    }

    public String getInitialPosition() {
        return state.getInitialPosition().getResultPosition().encode();
    }

    private ChessBoard makeMove(String initialPosition, CellCoords from, CellCoords to) {
        ChessBoard chessboard = new ChessBoard(initialPosition);
        chessboard.placePiece(to, chessboard.getPieceAt(from));
        chessboard.placePiece(from, null);
        return chessboard;
    }

    private Set<CellCoords> getPossibleMoves(CellCoords from) {
        final Move lastMove = state.getCurrMove();
        if (!lastMove.getNextMoves().isEmpty()) {
            return Collections.emptySet();
        }
        Chessman selectedChessman = lastMove.getResultPosition().getPieceAt(from);
        if (selectedChessman == null) {
            return Collections.emptySet();
        }
        ChessmanColor colorToMove = lastMove.getColorOfWhoMadeMove().inverse();
        if (!selectedChessman.getType().getPieceColor().equals(colorToMove)) {
            return Collections.emptySet();
        }
        return lastMove.getResultPosition().getPossibleMoves(from);
    }

    private ChessBoard getCurrentPosition() {
        return state.getCurrMove().getResultPosition();
    }

    private HistoryView toDto(Move initialPosition, final Move selectedMove) {
        HistoryView historyView = new HistoryView();
        int feMoveNumber = 1;
        MoveView moveView = new MoveView();
        moveView.setFeMoveNumber(feMoveNumber);
        Move currMove = getNextOnlyMove(initialPosition);
        while (currMove != null) {
            if (currMove.getColorOfWhoMadeMove().equals(ChessmanColor.WHITE)) {
                moveView.setWhitesMove(moveToString(currMove));
                moveView.setWhitesMoveSelected(selectedMove == currMove);
            } else {
                moveView.setBlacksMove(moveToString(currMove));
                moveView.setBlacksMoveSelected(selectedMove == currMove);

                historyView.getMoves().add(moveView);
                feMoveNumber++;
                moveView = new MoveView();
                moveView.setFeMoveNumber(feMoveNumber);
            }
            currMove = getNextOnlyMove(currMove);
        }
        if (moveView.getWhitesMove() != null) {
            historyView.getMoves().add(moveView);
        }
        return historyView;
    }

    private Move getNextOnlyMove(Move move) {
        List<Move> nextMoves = move.getNextMoves();
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
