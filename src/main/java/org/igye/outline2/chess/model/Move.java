package org.igye.outline2.chess.model;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.mapOf;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.igye.outline2.OutlineUtils.setOf;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_ROOK;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_ROOK;

public final class Move {
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
    private static final Map<ChessmanType, List<ChessmanType>> PAWN_REPLACEMENTS= mapOf(
            WHITE_PAWN, listOf(WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN),
            BLACK_PAWN, listOf(BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN)
    );

    @Getter
    private final CellCoords from;
    @Getter
    private final CellCoords to;
    private final ChessBoard resultPosition;
    private boolean whiteKingCastleAvailable = true;
    private boolean whiteQueenCastleAvailable = true;
    private boolean blackKingCastleAvailable = true;
    private boolean blackQueenCastleAvailable = true;

    private Move(Move prevMove, CellCoords from, CellCoords to, ChessBoard resultPosition) {
        this.from = from;
        this.to = to;
        this.resultPosition = resultPosition.clone();
        copyCastlingAbilities(prevMove, this);
    }

    public Move(CellCoords to, ChessBoard resultPosition) {
        from = null;
        this.to = to;
        this.resultPosition = resultPosition.clone();
    }

    public ChessmanColor getColorOfWhoMadeMove() {
        return resultPosition.getPieceAt(to).getType().getPieceColor();
    }

    public List<Move> getPossibleNextMoves(CellCoords from) {
        Chessman selectedChessman = resultPosition.getPieceAt(from);
        if (selectedChessman == null) {
            return Collections.emptyList();
        }
        ChessmanColor colorToMove = getColorOfWhoMadeMove().inverse();
        if (selectedChessman.getType().getPieceColor() != colorToMove) {
            return Collections.emptyList();
        }
        Set<CellCoords> possibleMoves = resultPosition.getPossibleMoves(from);
        possibleMoves.addAll(getPossibleCastlings(selectedChessman));
        possibleMoves.addAll(getPossibleEnPassant(from));
        return possibleMoves.stream()
                .flatMap(to -> makeMove(from, to).stream())
                .collect(Collectors.toList());
    }

    public ChessBoard getResultPosition() {
        return resultPosition.clone();
    }

    public ChessmanType getPieceAt(CellCoords coords) {
        return nullSafeGetter(resultPosition.getPieceAt(coords), Chessman::getType);
    }

    private Set<CellCoords> getPossibleCastlings(Chessman selectedChessman) {
        Set<CellCoords> result = new HashSet<>();
        if (selectedChessman.getType().equals(ChessmanType.WHITE_KING)) {
            if (
                    whiteQueenCastleAvailable
                            && isChessmanOnCell(WHITE_ROOK, A1)
                            && isChessmanOnCell(null, B1)
                            && isChessmanOnCell(null, C1)
                            && isChessmanOnCell(null, D1)
                            && isChessmanOnCell(ChessmanType.WHITE_KING, E1)
            ) {
                result.add(C1);
            }
            if (
                    whiteKingCastleAvailable
                            && isChessmanOnCell(ChessmanType.WHITE_KING, E1)
                            && isChessmanOnCell(null, F1)
                            && isChessmanOnCell(null, G1)
                            && isChessmanOnCell(WHITE_ROOK, H1)
            ) {
                result.add(G1);
            }
        } else if (selectedChessman.getType().equals(ChessmanType.BLACK_KING)) {
            if (
                    blackQueenCastleAvailable
                            && isChessmanOnCell(BLACK_ROOK, A8)
                            && isChessmanOnCell(null, B8)
                            && isChessmanOnCell(null, C8)
                            && isChessmanOnCell(null, D8)
                            && isChessmanOnCell(ChessmanType.BLACK_KING, E8)
            ) {
                result.add(C8);
            }
            if (
                    blackKingCastleAvailable
                            && isChessmanOnCell(ChessmanType.BLACK_KING, E8)
                            && isChessmanOnCell(null, F8)
                            && isChessmanOnCell(null, G8)
                            && isChessmanOnCell(BLACK_ROOK, H8)
            ) {
                result.add(G8);
            }
        }
        return result;
    }

    private Set<CellCoords> getPossibleEnPassant(CellCoords selectedCell) {
        if (from == null) {
            return Collections.emptySet();
        }
        final ChessmanType selectedPiece = resultPosition.getPieceAt(selectedCell).getType();
        final ChessmanType lastMovedPiece = resultPosition.getPieceAt(to).getType();
        if (selectedPiece == WHITE_PAWN
                && lastMovedPiece == ChessmanType.BLACK_PAWN
                && selectedCell.getY() == 4
                && from.getY() == 6 && to.getY() == 4
        ) {
            if (selectedCell.plusX(-1).equals(to)) {
                return setOf(selectedCell.plusXY(-1, 1));
            } else if (selectedCell.plusX(1).equals(to)) {
                return setOf(selectedCell.plusXY(1, 1));
            }
        } else if (selectedPiece == ChessmanType.BLACK_PAWN
                && lastMovedPiece == WHITE_PAWN
                && selectedCell.getY() == 3
                && from.getY() == 1 && to.getY() == 3
        ) {
            if (selectedCell.plusX(-1).equals(to)) {
                return setOf(selectedCell.plusXY(-1, -1));
            } else if (selectedCell.plusX(1).equals(to)) {
                return setOf(selectedCell.plusXY(1, -1));
            }
        }
        return Collections.emptySet();
    }

    private List<Move> makeMove(CellCoords from, CellCoords to) {
        ChessBoard chessboardAfterMove = resultPosition.clone();
        chessboardAfterMove.placePiece(to, chessboardAfterMove.getPieceAt(from));
        chessboardAfterMove.placePiece(from, null);
        processCastling(from, to, chessboardAfterMove);
        processEnPassant(resultPosition.clone(), from, to, chessboardAfterMove);
        return replacePawnOnBackRankIfNecessary(from, to, chessboardAfterMove);
    }

    private List<Move> replacePawnOnBackRankIfNecessary(CellCoords from, CellCoords to, ChessBoard chessboardAfterMove) {
        final ChessmanType movedPiece = chessboardAfterMove.getPieceAt(to).getType();
        if (movedPiece.getPieceShape() == PieceShape.PAWN && (to.getY() == 7 || to.getY() == 0)) {
            return map(PAWN_REPLACEMENTS.get(movedPiece), replacement -> {
                ChessBoard chessboardWithReplacement = chessboardAfterMove.clone();
                chessboardWithReplacement.placePiece(to, new Chessman(replacement));
                return new Move(this, from, to, chessboardWithReplacement);
            });
        } else {
            return listOf(new Move(this, from, to, chessboardAfterMove));
        }
    }

    private void processCastling(CellCoords from, CellCoords to, ChessBoard chessboardAfterMove) {
        if (isChessmanOnCell(chessboardAfterMove, ChessmanType.WHITE_KING, to)) {
            if (from.equals(E1) && to.equals(C1)) {
                chessboardAfterMove.placePiece(D1, chessboardAfterMove.getPieceAt(A1));
                chessboardAfterMove.placePiece(A1, null);
            } else if (from.equals(E1) && to.equals(G1)) {
                chessboardAfterMove.placePiece(F1, chessboardAfterMove.getPieceAt(H1));
                chessboardAfterMove.placePiece(H1, null);
            }
        }
        if (isChessmanOnCell(chessboardAfterMove, ChessmanType.BLACK_KING, to)) {
            if (from.equals(E8) && to.equals(C8)) {
                chessboardAfterMove.placePiece(D8, chessboardAfterMove.getPieceAt(A8));
                chessboardAfterMove.placePiece(A8, null);
            } else if (from.equals(E8) && to.equals(G8)) {
                chessboardAfterMove.placePiece(F8, chessboardAfterMove.getPieceAt(H8));
                chessboardAfterMove.placePiece(H8, null);
            }
        }
    }

    private void processEnPassant(ChessBoard chessBoardBeforeMove,
                                  CellCoords from, CellCoords to,
                                  ChessBoard chessboardAfterMove) {
        Chessman movedPiece = chessboardAfterMove.getPieceAt(to);
        if (movedPiece.getType().getPieceShape() == PieceShape.PAWN
                && (to.getX() == from.getX() - 1 || to.getX() == from.getX() + 1)
                && null == chessBoardBeforeMove.getPieceAt(to)) {
            chessboardAfterMove.placePiece(
                    to.plusY(movedPiece.getType().getPieceColor() == ChessmanColor.WHITE ? -1 : 1),
                    null
            );
        }
    }

    private void copyCastlingAbilities(Move prevMove, Move newMove) {
        newMove.whiteQueenCastleAvailable = prevMove.whiteQueenCastleAvailable
                        && isWhiteARookOnInitialCell(newMove)
                        && isWhiteKingOnInitialCell(newMove);

        newMove.whiteKingCastleAvailable = prevMove.whiteKingCastleAvailable
                        && isWhiteHRookOnInitialCell(newMove)
                        && isWhiteKingOnInitialCell(newMove);

        newMove.blackQueenCastleAvailable = prevMove.blackQueenCastleAvailable
                        && isBlackARookOnInitialCell(newMove)
                        && isBlackKingOnInitialCell(newMove);

        newMove.blackKingCastleAvailable = prevMove.blackKingCastleAvailable
                        && isBlackHRookOnInitialCell(newMove)
                        && isBlackKingOnInitialCell(newMove);

//        System.out.println("change of castling abilities ---------------------------------------------");
//        System.out.print("prevMove = " + ChessUtils.moveToString(prevMove));
//        System.out.println("   newMove = " + ChessUtils.moveToString(newMove));
//        System.out.println("whiteQueenCastleAvailable: " + prevMove.whiteQueenCastleAvailable + " -> " + newMove.whiteQueenCastleAvailable);
//        System.out.println("whiteKingCastleAvailable: " + prevMove.whiteKingCastleAvailable + " -> " + newMove.whiteKingCastleAvailable);
//        System.out.println("blackQueenCastleAvailable: " + prevMove.blackQueenCastleAvailable + " -> " + newMove.blackQueenCastleAvailable);
//        System.out.println("blackKingCastleAvailable: " + prevMove.blackKingCastleAvailable + " -> " + newMove.blackKingCastleAvailable);
    }

    private boolean isWhiteARookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, WHITE_ROOK, A1);
    }

    private boolean isWhiteHRookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, WHITE_ROOK, H1);
    }

    private boolean isWhiteKingOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, ChessmanType.WHITE_KING, E1);
    }

    private boolean isBlackARookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, BLACK_ROOK, A8);
    }

    private boolean isBlackHRookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, BLACK_ROOK, H8);
    }

    private boolean isBlackKingOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, ChessmanType.BLACK_KING, E8);
    }

    private boolean isChessmanOnCell(ChessmanType type, CellCoords coords) {
        return isChessmanOnCell(resultPosition, type, coords);
    }

    private boolean isChessmanOnCell(ChessBoard chessBoard, ChessmanType type, CellCoords coords) {
        final Chessman chessmanAtCoords = chessBoard.getPieceAt(coords);
        return type==null && chessmanAtCoords==null
                || chessmanAtCoords!=null && chessmanAtCoords.getType().equals(type);
    }
}
