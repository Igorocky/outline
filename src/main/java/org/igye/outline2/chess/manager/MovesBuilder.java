package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.ChoseChessmanTypeDialogView;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.MoveView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.Chessman;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.PieceShape;
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
        ChessComponentView chessComponentView = new ChessComponentView();
        chessComponentView.setChessBoard(ChessViewConverter.toDto(getCurrentPosition()));
        chessComponentView.setTab(ChessComponentStage.MOVES);
        final Move currMove = state.getCurrMove();
        if (state.getPreparedMoveFrom() != null) {
            chessComponentView.getChessBoard().setBorderColor(state.getPreparedMoveFrom(), PREPARED_TO_MOVE_COLOR);
            getPossibleMoves(state.getPreparedMoveFrom()).forEach(coords ->
                    chessComponentView.getChessBoard().setBorderColor(coords, AVAILABLE_TO_MOVE_TO_COLOR)
            );
        } else {
            if (currMove.getFrom() != null) {
                chessComponentView.getChessBoard().setBorderColor(currMove.getFrom(), PREPARED_TO_MOVE_COLOR);
            }
            if (currMove.getTo() != null) {
                chessComponentView.getChessBoard().setBorderColor(currMove.getTo(), AVAILABLE_TO_MOVE_TO_COLOR);
            }
        }
        chessComponentView.setHistory(toDto(state.getInitialPosition(), currMove));
        createChoseChessmanTypeDialogViewIfNecessary(chessComponentView);
        return chessComponentView;
    }

    private void createChoseChessmanTypeDialogViewIfNecessary(ChessComponentView chessComponentView) {
        if (state.isChoseChessmanTypeDialogOpenedForWhite()) {
            chessComponentView.setChoseChessmanTypeDialogView(
                    createChoseChessmanTypeDialogView(ChessmanColor.WHITE)
            );
        } else if (state.isChoseChessmanTypeDialogOpenedForBlack()) {
            chessComponentView.setChoseChessmanTypeDialogView(
                    createChoseChessmanTypeDialogView(ChessmanColor.BLACK)
            );
        }
    }

    @Override
    public ChessComponentView cellLeftClicked(CellCoords coordsClicked) {
        if (coordsClicked.getX() >= 20) {
            processPawnOnLastLine(coordsClicked);
            state.setChoseChessmanTypeDialogOpenedForWhite(false);
            state.setChoseChessmanTypeDialogOpenedForBlack(false);
        } else {
            final CellCoords preparedMoveFrom = state.getPreparedMoveFrom();
            if (preparedMoveFrom == null) {
                Set<CellCoords> possibleMoves = getPossibleMoves(coordsClicked);
                if (!possibleMoves.isEmpty()) {
                    state.setPreparedMoveFrom(coordsClicked);
                }
            } else if (coordsClicked.equals(preparedMoveFrom)) {
                state.setPreparedMoveFrom(null);
            } else {
                Set<CellCoords> possibleMoves = getPossibleMoves(preparedMoveFrom);
                if (possibleMoves.contains(coordsClicked)) {
                    final Move currMove = state.getCurrMove();
                    Move newMove = Move.builder()
                            .from(preparedMoveFrom)
                            .to(coordsClicked)
                            .resultPosition(makeMove(currMove.getResultPosition().encode(), preparedMoveFrom, coordsClicked))
                            .build();
                    copyCastlingAbilities(currMove, newMove);
                    currMove.getNextMoves().add(newMove);
                    state.setCurrMove(newMove);
                    state.setPreparedMoveFrom(null);
                    processPawnOnLastLine(newMove);
                } else {
                    state.setPreparedMoveFrom(null);
                    cellLeftClicked(coordsClicked);
                }
            }
        }
        return toView();
    }

    private void processPawnOnLastLine(CellCoords coordsClicked) {
        if (state.isChoseChessmanTypeDialogOpenedForWhite()) {
            final Move lastMove = state.getCurrMove();
            if (coordsClicked.getX() == 20) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.WHITE_KNIGHT));
            } else if (coordsClicked.getX() == 21) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.WHITE_BISHOP));
            } else if (coordsClicked.getX() == 22) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.WHITE_ROOK));
            } else if (coordsClicked.getX() == 23) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.WHITE_QUEEN));
            }
        } else if (state.isChoseChessmanTypeDialogOpenedForBlack()) {
            final Move lastMove = state.getCurrMove();
            if (coordsClicked.getX() == 20) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.BLACK_KNIGHT));
            } else if (coordsClicked.getX() == 21) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.BLACK_BISHOP));
            } else if (coordsClicked.getX() == 22) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.BLACK_ROOK));
            } else if (coordsClicked.getX() == 23) {
                lastMove.getResultPosition().placePiece(lastMove.getTo(), new Chessman(ChessmanType.BLACK_QUEEN));
            }
        }
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

    private void processPawnOnLastLine(Move newMove) {
        final Chessman pawn = newMove.getResultPosition().getPieceAt(newMove.getTo());
        if (pawn.getType().getPieceShape().equals(PieceShape.PAWN)) {
            if (pawn.getType().equals(ChessmanType.WHITE_PAWN) && newMove.getTo().getY() == 7) {
                state.setChoseChessmanTypeDialogOpenedForWhite(true);
            } else if (pawn.getType().equals(ChessmanType.BLACK_PAWN) && newMove.getTo().getY() == 0) {
                state.setChoseChessmanTypeDialogOpenedForBlack(true);
            }
        }
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
