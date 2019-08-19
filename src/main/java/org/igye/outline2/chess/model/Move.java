package org.igye.outline2.chess.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.exceptions.OutlineException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.mapOf;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.igye.outline2.OutlineUtils.nullSafeGetterWithDefault;
import static org.igye.outline2.OutlineUtils.setOf;
import static org.igye.outline2.chess.manager.ChessUtils.X_NAMES;
import static org.igye.outline2.chess.manager.ChessUtils.coordsToString;
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
    private static final Pattern MOVE_COMMAND_PATTERN = Pattern.compile("^([NBRQK]?)(?:([A-H])|([1-8]))?([A-H])([1-8])([NBRQ]?)$");
    @Getter
    private final Move prevMove;
    @Getter
    private final CellCoords from;
    @Getter
    private final CellCoords to;
    private final ChessBoard resultPosition;
    private boolean whiteKingCastleAvailable = true;
    private boolean whiteQueenCastleAvailable = true;
    private boolean blackKingCastleAvailable = true;
    private boolean blackQueenCastleAvailable = true;
    private String shortNotation;

    public Move(CellCoords to, ChessBoard resultPosition) {
        prevMove=null;
        from = null;
        this.to = to;
        this.resultPosition = resultPosition.clone();
    }

    private Move(Move prevMove, CellCoords from, CellCoords to, ChessBoard resultPosition) {
        this.prevMove = prevMove;
        this.from = from;
        this.to = to;
        this.resultPosition = resultPosition.clone();
        copyCastlingAbilities(prevMove, this);
    }

    public String getShortNotation() {
        if (shortNotation == null) {
            shortNotation = generateShortNotation(this);
        }
        return shortNotation;
    }

    public ChessmanColor getColorOfWhoMadeMove() {
        return resultPosition.getPieceAt(to).getPieceColor();
    }

    public ChessBoard getResultPosition() {
        return resultPosition.clone();
    }

    public ChessmanType getPieceAt(CellCoords coords) {
        return resultPosition.getPieceAt(coords);
    }

    public List<Move> getPossibleNextMoves(CellCoords from) {
        return getPossibleNextMoves(from, null);
    }

    public Set<CellCoords> findAll(Predicate<ChessmanType> predicate) {
        return resultPosition.findAll(predicate);
    }

    private Set<CellCoords> getAllCellsAttackedBy(ChessmanColor colorOfAttacker) {
        Set<CellCoords> attackers = new HashSet<>();
        resultPosition.traverse((x,y,piece) -> {
            if (piece.getPieceColor() == colorOfAttacker) {
                attackers.add(new CellCoords(x,y));
            }
            return true;
        });
        return attackers.stream()
                .flatMap(attacker -> getPossibleNextMoves(
                        attacker,
                        OPTIONS_TO_FIND_NEXT_MOVES_ALL_FALSE
                ).stream())
                .map(Move::getTo)
                .collect(Collectors.toSet());
    }

    public boolean isCheck() {
        final ChessmanColor colorOfOpponent = getColorOfWhoMadeMove().invert();
        return isCheckFor(colorOfOpponent);
    }

    public boolean isCheckFor(ChessmanColor colorOfKing) {
        CellCoords kingCoords = getKingCoords(colorOfKing);
        if (kingCoords == null) {
            return false;
        }
        return getAllCellsAttackedBy(colorOfKing.invert()).contains(kingCoords);
    }

    public boolean isStaleMate() {
        return !isCheck() && opponentHasNoMoves();
    }

    public boolean isCheckMate() {
        return isCheck() && opponentHasNoMoves();
    }

    public boolean isEnPassant() {
        return isEnPassant(prevMove.resultPosition, from, to, resultPosition);
    }

    public Move makeMove(String notation) throws ParseMoveException {
        String upperCaseNotation = notation.trim().toUpperCase();
        Matcher matcher = MOVE_COMMAND_PATTERN.matcher(upperCaseNotation);
        if (!matcher.matches()) {
            throw new ParseMoveException("Move notation format is incorrect.");
        }
        PieceShape pieceShape = nullSafeGetterWithDefault(
                matcher.group(1),
                s-> StringUtils.isBlank(s)?null:s,
                PieceShape::fromSymbol,
                PieceShape.PAWN
        );
        Integer coordFromX = strCoordToInt(matcher.group(2));
        Integer coordFromY = strCoordToInt(matcher.group(3));
        CellCoords cellToMoveTo = new CellCoords(
                strCoordToInt(matcher.group(4)),
                strCoordToInt(matcher.group(5))
        );
        PieceShape replacement = nullSafeGetter(
                matcher.group(6),
                s-> StringUtils.isBlank(s)?null:s,
                PieceShape::fromSymbol
        );

        ChessmanColor colorToMove = getColorOfWhoMadeMove().invert();
        Set<CellCoords> availableCellsFrom = findAll(c ->
                c.getPieceShape() == pieceShape && c.getPieceColor() == colorToMove
        );
        availableCellsFrom.removeIf(c ->
                coordFromX != null && c.getX() != coordFromX
                        || coordFromY != null && c.getY() != coordFromY
        );
        if (availableCellsFrom.isEmpty()) {
            throw new ParseMoveException("Cannot find specified piece to move.");
        }
        List<Pair<CellCoords, List<Move>>> possibleMoves = availableCellsFrom.stream()
                .map(c -> Pair.of(c, this.getPossibleNextMoves(c)))
                .map(p -> Pair.of(p, p.getRight().stream().map(Move::getTo).collect(Collectors.toSet())))
                .filter(p -> p.getRight().contains(cellToMoveTo))
                .map(pp -> pp.getLeft())
                .collect(Collectors.toList());
        if (possibleMoves.isEmpty()) {
            throw new ParseMoveException("Move is not possible.");
        }
        if (possibleMoves.size() > 1) {
            throw new ParseMoveException("Move is ambiguously specified.");
        }
        List<Move> result = possibleMoves.get(0).getRight().stream()
                .filter(m -> m.getTo().equals(cellToMoveTo))
                .collect(Collectors.toList());
        if (pieceShape == PieceShape.PAWN && (cellToMoveTo.getY() == 0 || cellToMoveTo.getY() == 7)) {
            if (replacement == null) {
                throw new ParseMoveException("Replacement is not specified.");
            }
            result.removeIf(m->m.resultPosition.getPieceAt(cellToMoveTo).getPieceShape() != replacement);
        }
        if (result.size() != 1) {
            throw new OutlineException("result.size() != 1");
        }
        return result.get(0);
    }

    public boolean isShortCastling() {
        return isCastling() && to.getX() == 6;
    }

    public boolean isLongCastling() {
        return isCastling() && to.getX() == 2;
    }

    private CellCoords getKingCoords(ChessmanColor colorOfKing) {
        return resultPosition.findFirstCoords(chessman ->
                chessman.getPieceColor() == colorOfKing
                        && chessman.getPieceShape() == PieceShape.KING
        );
    }

    private boolean opponentHasNoMoves() {
        ChessmanColor colorOfOpponent = getColorOfWhoMadeMove().invert();
        Set<CellCoords> allOpponentsChessmen = findAll(cm -> cm.getPieceColor() == colorOfOpponent);
        allOpponentsChessmen.removeIf(c -> getPossibleNextMoves(c).isEmpty());
        return allOpponentsChessmen.isEmpty();
    }

    private Integer strCoordToInt(String strCoord) {
        if (strCoord == null) {
            return null;
        }
        int coordCode = strCoord.charAt(0);
        if (coordCode >= 65) {
            return coordCode - 65;
        } else {
            return coordCode - 49;
        }
    }

    private boolean isCastling() {
        return from != null
                && resultPosition.getPieceAt(to).getPieceShape() == PieceShape.KING
                && (Math.abs(from.getX()-to.getX()) > 1 || Math.abs(from.getY()-to.getY()) > 1);
    }

    private List<Move> getPossibleNextMoves(CellCoords from, OptionsToFindNextMoves options) {
        ChessmanType selectedChessman = resultPosition.getPieceAt(from);
        if (selectedChessman == null) {
            return Collections.emptyList();
        }
        ChessmanColor colorToMove = getColorOfWhoMadeMove().invert();
        if ((options == null || options.isCheckColor()) && selectedChessman.getPieceColor() != colorToMove) {
            return Collections.emptyList();
        }
        Set<CellCoords> possibleMoves = resultPosition.getPossibleMoves(from);
        if (options == null || options.isCheckPossibleCastlings()) {
            possibleMoves.addAll(getPossibleCastlings(selectedChessman));
        }
        possibleMoves.addAll(getPossibleEnPassant(from));
        List<Move> possibleNextMoves = possibleMoves.stream()
                .flatMap(to -> makeMove(from, to).stream())
                .collect(Collectors.toList());
        if (options == null || options.isPerformSelfCheckValidation()) {
            possibleNextMoves.removeIf(move -> move.isCheckFor(colorToMove));
        }
        return possibleNextMoves;
    }

    private Set<CellCoords> getPossibleCastlings(ChessmanType selectedChessman) {
        Set<CellCoords> result = new HashSet<>();
        Set<CellCoords> cellsUnderAttack = null;
        if (selectedChessman.equals(ChessmanType.WHITE_KING)) {
            if (
                    whiteQueenCastleAvailable
                            && isChessmanOnCell(WHITE_ROOK, A1)
                            && isChessmanOnCell(null, B1)
                            && isChessmanOnCell(null, C1)
                            && isChessmanOnCell(null, D1)
                            && isChessmanOnCell(ChessmanType.WHITE_KING, E1)
            ) {
                if (cellsUnderAttack == null) {
                    cellsUnderAttack = getAllCellsAttackedBy(ChessmanColor.BLACK);
                }
                if (!cellsUnderAttack.contains(D1) && !cellsUnderAttack.contains(C1) && !cellsUnderAttack.contains(E1)) {
                    result.add(C1);
                }
            }
            if (
                    whiteKingCastleAvailable
                            && isChessmanOnCell(ChessmanType.WHITE_KING, E1)
                            && isChessmanOnCell(null, F1)
                            && isChessmanOnCell(null, G1)
                            && isChessmanOnCell(WHITE_ROOK, H1)
            ) {
                if (cellsUnderAttack == null) {
                    cellsUnderAttack = getAllCellsAttackedBy(ChessmanColor.BLACK);
                }
                if (!cellsUnderAttack.contains(F1) && !cellsUnderAttack.contains(G1) && !cellsUnderAttack.contains(E1)) {
                    result.add(G1);
                }
            }
        } else if (selectedChessman.equals(ChessmanType.BLACK_KING)) {
            if (
                    blackQueenCastleAvailable
                            && isChessmanOnCell(BLACK_ROOK, A8)
                            && isChessmanOnCell(null, B8)
                            && isChessmanOnCell(null, C8)
                            && isChessmanOnCell(null, D8)
                            && isChessmanOnCell(ChessmanType.BLACK_KING, E8)
            ) {
                if (cellsUnderAttack == null) {
                    cellsUnderAttack = getAllCellsAttackedBy(ChessmanColor.WHITE);
                }
                if (!cellsUnderAttack.contains(C8) && !cellsUnderAttack.contains(D8) && !cellsUnderAttack.contains(E8)) {
                    result.add(C8);
                }
            }
            if (
                    blackKingCastleAvailable
                            && isChessmanOnCell(ChessmanType.BLACK_KING, E8)
                            && isChessmanOnCell(null, F8)
                            && isChessmanOnCell(null, G8)
                            && isChessmanOnCell(BLACK_ROOK, H8)
            ) {
                if (cellsUnderAttack == null) {
                    cellsUnderAttack = getAllCellsAttackedBy(ChessmanColor.WHITE);
                }
                if (!cellsUnderAttack.contains(F8) && !cellsUnderAttack.contains(G8) && !cellsUnderAttack.contains(E8)) {
                    result.add(G8);
                }
            }
        }
        return result;
    }

    private Set<CellCoords> getPossibleEnPassant(CellCoords selectedCell) {
        if (from == null) {
            return Collections.emptySet();
        }
        final ChessmanType selectedPiece = resultPosition.getPieceAt(selectedCell);
        final ChessmanType lastMovedPiece = resultPosition.getPieceAt(to);
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
        final ChessmanType movedPiece = chessboardAfterMove.getPieceAt(to);
        if (movedPiece.getPieceShape() == PieceShape.PAWN && (to.getY() == 7 || to.getY() == 0)) {
            return map(PAWN_REPLACEMENTS.get(movedPiece), replacement -> {
                ChessBoard chessboardWithReplacement = chessboardAfterMove.clone();
                chessboardWithReplacement.placePiece(to, replacement);
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

    private boolean isEnPassant(ChessBoard chessBoardBeforeMove,
                                  CellCoords from, CellCoords to,
                                  ChessBoard chessboardAfterMove) {
        ChessmanType movedPiece = chessboardAfterMove.getPieceAt(to);
        return movedPiece.getPieceShape() == PieceShape.PAWN
                && (to.getX() == from.getX() - 1 || to.getX() == from.getX() + 1)
                && null == chessBoardBeforeMove.getPieceAt(to);
    }

    private void processEnPassant(ChessBoard chessBoardBeforeMove,
                                  CellCoords from, CellCoords to,
                                  ChessBoard chessboardAfterMove) {
        if (isEnPassant(chessBoardBeforeMove, from, to, chessboardAfterMove)) {
            chessboardAfterMove.placePiece(
                    to.plusY(chessboardAfterMove.getPieceAt(to).getPieceColor() == ChessmanColor.WHITE ? -1 : 1),
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
        final ChessmanType chessmanAtCoords = chessBoard.getPieceAt(coords);
        return type==null && chessmanAtCoords==null
                || chessmanAtCoords!=null && chessmanAtCoords.equals(type);
    }

    private static String generateShortNotation(Move move) {
        if (move.getFrom() == null) {
            return "start-position";
        } else if (move.isShortCastling()) {
            return "0-0";
        } else if (move.isLongCastling()) {
            return "0-0-0";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(whatMoved(move));
        if (wasCapture(move)) {
            sb.append("x");
        }
        sb.append(coordsToString(move.getTo()));
        if (wasPawnChange(move)) {
            sb.append(chessmanShapeToShortSymbol(move.getPieceAt(move.getTo())));
        }
        if (move.isEnPassant()) {
            sb.append("e.p.");
        }
        if (move.isCheckMate()) {
            sb.append("#");
        } else if (move.isStaleMate()) {
            sb.append("=");
        } else if (move.isCheck()) {
            sb.append("+");
        }
        return sb.toString();
    }

    private static boolean wasPawnChange(Move move) {
        return move.getPrevMove().getPieceAt(move.getFrom()).getPieceShape() == PieceShape.PAWN
                && move.getPieceAt(move.getTo()).getPieceShape() != PieceShape.PAWN;
    }

    private static boolean wasCapture(Move move) {
        return move.getPrevMove().getPieceAt(move.getTo()) != null;
    }

    private static String whatMoved(Move move) {
        ChessmanType movedPiece = move.getPrevMove().getPieceAt(move.getFrom());
        Set<CellCoords> coordsOfAllPiecesWithSameShape =
                move.getPrevMove().findAll(piece ->
                        piece.getPieceShape() == movedPiece.getPieceShape()
                        && piece.getPieceColor() == movedPiece.getPieceColor()
                );
        if (coordsOfAllPiecesWithSameShape.size() == 1) {
            return chessmanShapeToShortSymbol(movedPiece);
        }
        List<CellCoords> moveCandidates = coordsOfAllPiecesWithSameShape.stream()
                .map(from -> Pair.of(
                        from,
                        move.getPrevMove().getPossibleNextMoves(from).stream()
                                .map(Move::getTo)
                                .collect(Collectors.toSet())
                ))
                .filter(pair -> pair.getRight().contains(move.getTo()))
                .map(Pair::getLeft)
                .collect(Collectors.toList());
        if (moveCandidates.size() == 1) {
            return chessmanShapeToShortSymbol(movedPiece);
        }
        return chessmanShapeToShortSymbol(movedPiece) + getUniqueCoord(move.getFrom(), moveCandidates);
    }

    private static String chessmanShapeToShortSymbol(ChessmanType type) {
        final PieceShape pieceShape = type.getPieceShape();
        if (pieceShape == PieceShape.PAWN) {
            return "";
        } else {
            return pieceShape.getSymbol();
        }
    }

    private static String getUniqueCoord(CellCoords from, List<CellCoords> moveCandidates) {
        if (1 == countCoordsWithSameComponent(from, moveCandidates, CellCoords::getX)) {
            return String.valueOf(X_NAMES.charAt(from.getX()));
        }
        if (1 == countCoordsWithSameComponent(from, moveCandidates, CellCoords::getY)) {
            return String.valueOf(from.getY()+1);
        }
        return coordsToString(from);
    }

    private static long countCoordsWithSameComponent(CellCoords from,
                                                     List<CellCoords> moveCandidates,
                                                     Function<CellCoords,Integer> componentExtractor) {
        return moveCandidates.stream()
                .filter(coord -> componentExtractor.apply(coord) == componentExtractor.apply(from))
                .count();
    }

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
    private static final OptionsToFindNextMoves OPTIONS_TO_FIND_NEXT_MOVES_ALL_FALSE = OptionsToFindNextMoves.builder()
            .checkColor(false)
            .checkPossibleCastlings(false)
            .performSelfCheckValidation(false)
            .build();
}
