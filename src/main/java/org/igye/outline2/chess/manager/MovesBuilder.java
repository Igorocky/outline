package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.MoveView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.Chessman;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.exceptions.OutlineException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.igye.outline2.chess.manager.ChessUtils.moveToString;

public class MovesBuilder implements ChessComponentStateManager {
    private static final String AVAILABLE_TO_MOVE_TO_COLOR = "green";
    private static final String PREPARED_TO_MOVE_COLOR = "yellow";
    private static final CellCoords A1 = new CellCoords(0, 0);
    private static final CellCoords B1 = new CellCoords(1, 0);
    private static final CellCoords C1 = new CellCoords(2, 0);
    private static final CellCoords D1 = new CellCoords(3, 0);
    private static final CellCoords E1 = new CellCoords(4, 0);
    private static final CellCoords F1 = new CellCoords(5, 0);
    private static final CellCoords G1 = new CellCoords(6, 0);
    private static final CellCoords H1 = new CellCoords(7, 0);
    private static final CellCoords A8 = new CellCoords(0, 7);
    private static final CellCoords B8 = new CellCoords(1, 7);
    private static final CellCoords C8 = new CellCoords(2, 7);
    private static final CellCoords D8 = new CellCoords(3, 7);
    private static final CellCoords E8 = new CellCoords(4, 7);
    private static final CellCoords F8 = new CellCoords(5, 7);
    private static final CellCoords G8 = new CellCoords(6, 7);
    private static final CellCoords H8 = new CellCoords(7, 7);
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
        final Move currMove = state.getCurrMove();
        if (state.getPreparedMoveFrom() != null) {
            result.getChessBoard().setBorderColor(state.getPreparedMoveFrom(), PREPARED_TO_MOVE_COLOR);
            getPossibleMoves(state.getPreparedMoveFrom()).forEach(coords ->
                    result.getChessBoard().setBorderColor(coords, AVAILABLE_TO_MOVE_TO_COLOR)
            );
        } else {
            if (currMove.getFrom() != null) {
                result.getChessBoard().setBorderColor(currMove.getFrom(), PREPARED_TO_MOVE_COLOR);
            }
            if (currMove.getTo() != null) {
                result.getChessBoard().setBorderColor(currMove.getTo(), AVAILABLE_TO_MOVE_TO_COLOR);
            }
        }
        result.setHistory(toDto(state.getInitialPosition(), currMove));
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
                copyCastlingAbilities(currMove, newMove);
                currMove.getNextMoves().add(newMove);
                state.setCurrMove(newMove);
                state.setPreparedMoveFrom(null);
            } else {
                state.setPreparedMoveFrom(null);
                cellLeftClicked(coords);
            }
        }
        return toView();
    }

    public String getInitialPosition() {
        return state.getInitialPosition().getResultPosition().encode();
    }

    private void copyCastlingAbilities(Move prevMove, Move newMove) {
        newMove.setWhiteQueenCastleAvailable(
                prevMove.isWhiteQueenCastleAvailable()
                && isWhiteARookOnInitialCell(newMove)
                && isWhiteKingOnInitialCell(newMove)
        );
        newMove.setWhiteKingCastleAvailable(
                prevMove.isWhiteKingCastleAvailable()
                && isWhiteHRookOnInitialCell(newMove)
                && isWhiteKingOnInitialCell(newMove)
        );
        newMove.setBlackQueenCastleAvailable(
                prevMove.isBlackQueenCastleAvailable()
                && isBlackARookOnInitialCell(newMove)
                && isBlackKingOnInitialCell(newMove)
        );
        newMove.setBlackKingCastleAvailable(
                prevMove.isBlackKingCastleAvailable()
                && isBlackHRookOnInitialCell(newMove)
                && isBlackKingOnInitialCell(newMove)
        );
    }

    private boolean isWhiteARookOnInitialCell(Move move) {
        return isChessmanOnCell(move, ChessmanType.WHITE_ROOK, A1);
    }

    private boolean isWhiteHRookOnInitialCell(Move move) {
        return isChessmanOnCell(move, ChessmanType.WHITE_ROOK, H1);
    }

    private boolean isWhiteKingOnInitialCell(Move move) {
        return isChessmanOnCell(move, ChessmanType.WHITE_KING, E1);
    }

    private boolean isBlackARookOnInitialCell(Move move) {
        return isChessmanOnCell(move, ChessmanType.BLACK_ROOK, A8);
    }

    private boolean isBlackHRookOnInitialCell(Move move) {
        return isChessmanOnCell(move, ChessmanType.BLACK_ROOK, H8);
    }

    private boolean isBlackKingOnInitialCell(Move move) {
        return isChessmanOnCell(move, ChessmanType.BLACK_KING, E8);
    }

    private boolean isChessmanOnCell(Move move, ChessmanType type, CellCoords coords) {
        return isChessmanOnCell(move.getResultPosition(), type, coords);
    }

    private boolean isChessmanOnCell(ChessBoard chessBoard, ChessmanType type, CellCoords coords) {
        final Chessman chessmanAtCoords = chessBoard.getPieceAt(coords);
        return type==null && chessmanAtCoords==null
                || chessmanAtCoords!=null && chessmanAtCoords.getType().equals(type);
    }

    private ChessBoard makeMove(String initialPosition, CellCoords from, CellCoords to) {
        ChessBoard chessboard = new ChessBoard(initialPosition);
        chessboard.placePiece(to, chessboard.getPieceAt(from));
        chessboard.placePiece(from, null);
        processCastling(from, to, chessboard);
        processEnPassant(new ChessBoard(initialPosition), from, to, chessboard);
        return chessboard;
    }

    private void processEnPassant(ChessBoard chessBoardBeforeMove,
                                  CellCoords from, CellCoords to,
                                  ChessBoard chessboardAfterMove) {
        if (isChessmanOnCell(chessboardAfterMove, ChessmanType.WHITE_PAWN, to)) {
            if (null == chessBoardBeforeMove.getPieceAt(to)) {
                if (to.getX() == from.getX() - 1 || to.getX() == from.getX() + 1) {
                    chessboardAfterMove.placePiece(to.plusY(-1), null);
                }
            }
        } else if (isChessmanOnCell(chessboardAfterMove, ChessmanType.BLACK_PAWN, to)) {
            if (null == chessBoardBeforeMove.getPieceAt(to)) {
                if (to.getX() == from.getX() - 1 || to.getX() == from.getX() + 1) {
                    chessboardAfterMove.placePiece(to.plusY(1), null);
                }
            }
        }
    }

    private void processCastling(CellCoords from, CellCoords to, ChessBoard chessboardAfterMove) {
        if (from.equals(E1) && to.equals(C1)
                        && isChessmanOnCell(chessboardAfterMove, ChessmanType.WHITE_KING, to)) {
            chessboardAfterMove.placePiece(D1, chessboardAfterMove.getPieceAt(A1));
            chessboardAfterMove.placePiece(A1, null);
        }
        if (from.equals(E1) && to.equals(G1)
                        && isChessmanOnCell(chessboardAfterMove, ChessmanType.WHITE_KING, to)) {
            chessboardAfterMove.placePiece(F1, chessboardAfterMove.getPieceAt(H1));
            chessboardAfterMove.placePiece(H1, null);
        }
        if (from.equals(E8) && to.equals(C8)
                        && isChessmanOnCell(chessboardAfterMove, ChessmanType.BLACK_KING, to)) {
            chessboardAfterMove.placePiece(D8, chessboardAfterMove.getPieceAt(A8));
            chessboardAfterMove.placePiece(A8, null);
        }
        if (from.equals(E8) && to.equals(G8)
                        && isChessmanOnCell(chessboardAfterMove, ChessmanType.BLACK_KING, to)) {
            chessboardAfterMove.placePiece(F8, chessboardAfterMove.getPieceAt(H8));
            chessboardAfterMove.placePiece(H8, null);
        }
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
        Set<CellCoords> possibleMoves = lastMove.getResultPosition().getPossibleMoves(from);
        possibleMoves.addAll(getPossibleCastlings(lastMove, selectedChessman));
        possibleMoves.addAll(getPossibleEnPassant(lastMove, from));
        return possibleMoves;
    }

    private Set<CellCoords> getPossibleEnPassant(Move move, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        if (move.getResultPosition().getPieceAt(from).getType().equals(ChessmanType.WHITE_PAWN)
                && from.getY() == 4
                && move.getResultPosition().getPieceAt(move.getTo()).getType().equals(ChessmanType.BLACK_PAWN)
                && move.getFrom() != null
                && move.getFrom().getY() == 6 && move.getTo().getY() == 4
        ) {
            if (move.getFrom().getX() == from.getX() - 1 && move.getTo().getX() == from.getX() - 1) {
                result.add(from.plusX(-1).plusY(1));
            } else if (move.getFrom().getX() == from.getX() + 1 && move.getTo().getX() == from.getX() + 1) {
                result.add(from.plusX(1).plusY(1));
            }
        } else if (move.getResultPosition().getPieceAt(from).getType().equals(ChessmanType.BLACK_PAWN)
                && from.getY() == 3
                && move.getResultPosition().getPieceAt(move.getTo()).getType().equals(ChessmanType.WHITE_PAWN)
                && move.getFrom() != null
                && move.getFrom().getY() == 1 && move.getTo().getY() == 3
        ) {
            if (move.getFrom().getX() == from.getX() - 1 && move.getTo().getX() == from.getX() - 1) {
                result.add(from.plusX(-1).plusY(-1));
            } else if (move.getFrom().getX() == from.getX() + 1 && move.getTo().getX() == from.getX() + 1) {
                result.add(from.plusX(1).plusY(-1));
            }
        }
        return result;
    }

    private Set<CellCoords> getPossibleCastlings(Move move, Chessman selectedChessman) {
        Set<CellCoords> result = new HashSet<>();
        if (selectedChessman.getType().equals(ChessmanType.WHITE_KING)) {
            if (
                    move.isWhiteQueenCastleAvailable()
                        && isChessmanOnCell(move, ChessmanType.WHITE_ROOK, A1)
                        && isChessmanOnCell(move, null, B1)
                        && isChessmanOnCell(move, null, C1)
                        && isChessmanOnCell(move, null, D1)
                        && isChessmanOnCell(move, ChessmanType.WHITE_KING, E1)
            ) {
                result.add(C1);
            }
            if (
                    move.isWhiteKingCastleAvailable()
                        && isChessmanOnCell(move, ChessmanType.WHITE_KING, E1)
                        && isChessmanOnCell(move, null, F1)
                        && isChessmanOnCell(move, null, G1)
                        && isChessmanOnCell(move, ChessmanType.WHITE_ROOK, H1)
            ) {
                result.add(G1);
            }
        } else if (selectedChessman.getType().equals(ChessmanType.BLACK_KING)) {
            if (
                    move.isBlackQueenCastleAvailable()
                        && isChessmanOnCell(move, ChessmanType.BLACK_ROOK, A8)
                        && isChessmanOnCell(move, null, B8)
                        && isChessmanOnCell(move, null, C8)
                        && isChessmanOnCell(move, null, D8)
                        && isChessmanOnCell(move, ChessmanType.BLACK_KING, E8)
            ) {
                result.add(C8);
            }
            if (
                    move.isBlackKingCastleAvailable()
                        && isChessmanOnCell(move, ChessmanType.BLACK_KING, E8)
                        && isChessmanOnCell(move, null, F8)
                        && isChessmanOnCell(move, null, G8)
                        && isChessmanOnCell(move, ChessmanType.BLACK_ROOK, H8)
            ) {
                result.add(G8);
            }
        }
        return result;
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
