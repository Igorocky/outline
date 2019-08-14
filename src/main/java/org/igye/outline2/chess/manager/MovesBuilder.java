package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.MoveView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.Chessman;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.History;
import org.igye.outline2.chess.model.Move;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MovesBuilder implements ChessComponentStateManager {
    private static final String X_NAMES = "abcdefgh";

    private History history;
    private int currMoveNumber;
    private Move preparedMove;

    public MovesBuilder(ChessmanColor lastMovedColor, String initialPosition) {
        history = new History(lastMovedColor, new ChessBoard(initialPosition));
    }

    @Override
    public ChessComponentView toView() {
        ChessComponentView result = new ChessComponentView();
        result.setChessBoard(ChessViewConverter.toDto(getCurrentPosition()));
        result.setTab(ChessComponentStage.MOVES);
        if (preparedMove != null) {
            Set<Pair<Integer, Integer>> cellsToSelect = new HashSet<>();
            cellsToSelect.add(Pair.of(preparedMove.getFrom().getX(), preparedMove.getFrom().getY()));
            getPossibleMoves(preparedMove.getFrom()).forEach(coords ->
                    cellsToSelect.add(Pair.of(coords.getX(), coords.getY()))
            );
            cellsToSelect.forEach(coords->
                    result.getChessBoard().getCells()
                            .get(coords.getLeft())
                            .get(coords.getRight())
                            .setBackgroundColor("green")
            );
        }
        result.setHistory(toDto(history, currMoveNumber));
        return result;
    }

    @Override
    public ChessComponentView cellLeftClicked(CellCoords coords) {
        final Move lastMove = getLastMove();
        if (preparedMove == null) {
            Set<CellCoords> possibleMoves = getPossibleMoves(coords);
            if (!possibleMoves.isEmpty()) {
                preparedMove = Move.builder()
                        .color(lastMove.getColor().inverse())
                        .from(coords)
                        .build();
            }
        } else {
            Set<CellCoords> possibleMoves = getPossibleMoves(preparedMove.getFrom());
            if (possibleMoves.contains(coords)) {
                preparedMove.setTo(coords);
                preparedMove.setResultPosition(makeMove(
                        lastMove.getResultPosition().encode(),
                        preparedMove.getFrom(),
                        preparedMove.getTo()
                ));
                history.getMoves().add(preparedMove);
                currMoveNumber++;
                preparedMove = null;
            } else {
                preparedMove = null;
                cellLeftClicked(coords);
            }
        }
        return toView();
    }

    public String getInitialPosition() {
        return history.getMoves().get(0).getResultPosition().encode();
    }

    private ChessBoard makeMove(String initialPosition, CellCoords from, CellCoords to) {
        ChessBoard chessboard = new ChessBoard(initialPosition);
        chessboard.placePiece(to, chessboard.getPieceAt(from));
        chessboard.placePiece(from, null);
        return chessboard;
    }

    private Set<CellCoords> getPossibleMoves(CellCoords from) {
        if (currMoveNumber != history.getMoves().size()-1) {
            return Collections.emptySet();
        }
        Move lastMove = getLastMove();
        Chessman selectedChessman = lastMove.getResultPosition().getPieceAt(from);
        if (selectedChessman == null) {
            return Collections.emptySet();
        }
        ChessmanColor colorToMove = lastMove.getColor().inverse();
        if (!selectedChessman.getType().getPieceColor().equals(colorToMove)) {
            return Collections.emptySet();
        }
        return lastMove.getResultPosition().getPossibleMoves(from);
    }

    private ChessBoard getCurrentPosition() {
        return history.getMoves().get(currMoveNumber).getResultPosition();
    }

    private Move getLastMove() {
        return history.getMoves().get(history.getMoves().size()-1);
    }

    private HistoryView toDto(History history, int currMoveNumber) {
        HistoryView historyView = new HistoryView();
        int feMoveNumber = 1;
        MoveView moveView = new MoveView();
        moveView.setFeMoveNumber(feMoveNumber);
        for (int i = 1; i < history.getMoves().size(); i++) {
            Move move = history.getMoves().get(i);
            if (move.getColor().equals(ChessmanColor.WHITE)) {
                moveView.setWhitesMove(moveToString(move));
                moveView.setWhitesMoveSelected(currMoveNumber == i);
            } else {
                moveView.setBlacksMove(moveToString(move));
                moveView.setBlacksMoveSelected(currMoveNumber == i);
                historyView.getMoves().add(moveView);

                feMoveNumber++;
                moveView = new MoveView();
                moveView.setFeMoveNumber(feMoveNumber);
            }
        }
        if (moveView.getWhitesMove() != null) {
            historyView.getMoves().add(moveView);
        }
        return historyView;
    }

    private String moveToString(Move move) {
        return coordsToString(move.getFrom()) + "-" + coordsToString(move.getTo());
    }

    private String coordsToString(CellCoords coords) {
        return String.valueOf(X_NAMES.charAt(coords.getX())) + (coords.getY()+1);
    }
}
