package org.igye.outline2.chess.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.common.OutlineUtils;
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

import static org.igye.outline2.chess.manager.ChessUtils.X_NAMES;
import static org.igye.outline2.chess.manager.ChessUtils.coordsToString;
import static org.igye.outline2.chess.manager.ChessUtils.strCoordToInt;
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
import static org.igye.outline2.common.OutlineUtils.listOf;
import static org.igye.outline2.common.OutlineUtils.map;
import static org.igye.outline2.common.OutlineUtils.mapOf;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetter;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetterWithDefault;
import static org.igye.outline2.common.OutlineUtils.setOf;

public final class Move {
    private static final Pattern MOVE_COMMAND_PATTERN =
            Pattern.compile("^([NBRQK]?)(?:([a-h])|([1-8]))?(?:x)?([a-h])([1-8])(?:=?([NBRQ]))?([+#]?)$");
    @Getter
    private final Move prevMove;
    @Getter
    private final CellCoords from;
    @Getter
    private final CellCoords to;
    private final ChessmanColor colorToMove;
    private final ChessBoard resultPosition;
    @Getter
    private boolean whiteKingCastleAvailable = true;
    @Getter
    private boolean whiteQueenCastleAvailable = true;
    @Getter
    private boolean blackKingCastleAvailable = true;
    @Getter
    private boolean blackQueenCastleAvailable = true;
    private final String enPassantTargetSquare;
    @Getter
    private final int halfmoveClock;
    @Getter
    private final int fullmoveNumber;
    private String shortNotation;

    public Move(String fen) {
        this(new ChessBoard(fen), getColorToMove(fen), getEnPassantTargetSquare(fen),
                getHalfmoveClock(fen), getFullmoveClock(fen));
        setCastlingAvailabilities(fen);
    }

    public Move(ChessBoard resultPosition, ChessmanColor colorToMove) {
        this(resultPosition, colorToMove, "-", 0, 1);
    }

    public Move(ChessBoard resultPosition, ChessmanColor colorToMove,
                        boolean whiteKingCastleAvailable,
                        boolean whiteQueenCastleAvailable,
                        boolean blackKingCastleAvailable,
                        boolean blackQueenCastleAvailable,
                        String enPassantTargetSquare,
                        int halfmoveClock,
                        int fullmoveNumber) {
        this(resultPosition, colorToMove, enPassantTargetSquare, halfmoveClock, fullmoveNumber);
        this.whiteKingCastleAvailable = whiteKingCastleAvailable;
        this.whiteQueenCastleAvailable = whiteQueenCastleAvailable;
        this.blackKingCastleAvailable = blackKingCastleAvailable;
        this.blackQueenCastleAvailable = blackQueenCastleAvailable;
    }

    public Move(ChessBoard resultPosition, ChessmanColor colorToMove,
                String enPassantTargetSquare, int halfmoveClock, int fullmoveNumber) {
        prevMove=null;
        from = null;
        to = null;
        this.colorToMove = colorToMove;
        this.resultPosition = resultPosition.clone();
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
        copyCastlingAbilities(prevMove, this);
        this.enPassantTargetSquare = enPassantTargetSquare;
    }

    private Move(Move prevMove, CellCoords from, CellCoords to, ChessBoard resultPosition) {
        this.prevMove = prevMove;
        this.from = from;
        this.to = to;
        this.colorToMove = prevMove.getColorOfWhoToMove().invert();
        this.resultPosition = resultPosition.clone();
        halfmoveClock = calcHalfmoveClock(prevMove, to, this.resultPosition);
        fullmoveNumber = calcFullmoveNumber(prevMove, to, this.resultPosition);
        copyCastlingAbilities(prevMove, this);
        enPassantTargetSquare = "-";
    }

    public String getShortNotation() {
        if (shortNotation == null) {
            shortNotation = generateShortNotation(this);
        }
        return shortNotation;
    }

    public ChessmanColor getColorOfWhoMadeMove() {
        return getColorOfWhoToMove().invert();
    }

    public ChessmanColor getColorOfWhoToMove() {
        return colorToMove;
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

    public Set<CellCoords> getAllCellsAttackedBy(ChessmanColor colorOfAttacker) {
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
        MoveNotationParts moveNotationParts = new MoveNotationParts();
        moveNotationParts.setNotation(notation);
        if ("0-0".equals(notation) || "O-O".equals(notation)) {
            moveNotationParts.setPieceShape(PieceShape.KING);
            if (getColorOfWhoToMove() == ChessmanColor.WHITE) {
                moveNotationParts.setCellToMoveTo(G1);
            } else {
                moveNotationParts.setCellToMoveTo(G8);
            }
        } else if ("0-0-0".equals(notation) || "O-O-O".equals(notation)) {
            moveNotationParts.setPieceShape(PieceShape.KING);
            if (getColorOfWhoToMove() == ChessmanColor.WHITE) {
                moveNotationParts.setCellToMoveTo(C1);
            } else {
                moveNotationParts.setCellToMoveTo(C8);
            }
        } else {
            String trimmedNotation = notation.trim();
            Matcher matcher = MOVE_COMMAND_PATTERN.matcher(trimmedNotation);
            if (!matcher.matches()) {
                throw new ParseMoveException("'" + notation + "' - could not recognize move notation format.");
            }
            moveNotationParts.setPieceShape(
                    nullSafeGetterWithDefault(
                            matcher.group(1),
                            s-> StringUtils.isBlank(s)?null:s,
                            PieceShape::fromSymbol,
                            PieceShape.PAWN
                    )
            );
            moveNotationParts.setCoordFromX(strCoordToInt(matcher.group(2)));
            moveNotationParts.setCoordFromY(strCoordToInt(matcher.group(3)));
            moveNotationParts.setCellToMoveTo(
                    new CellCoords(
                            strCoordToInt(matcher.group(4)),
                            strCoordToInt(matcher.group(5))
                    )
            );
            moveNotationParts.setReplacement(
                    nullSafeGetter(
                            matcher.group(6),
                            s-> (StringUtils.isBlank(s) || "+".equals(s) || "#".equals(s))?null:s,
                            PieceShape::fromSymbol
                    )
            );
        }
        return makeMove(moveNotationParts);
    }

    public Move makeMove(CellCoords from, CellCoords to, PieceShape promotion) {
        return OutlineUtils.getSingleValueOrNull(
                getPossibleNextMoves(from).stream()
                        .filter(
                                mv -> mv.getTo().equals(to)
                                        && (promotion == null
                                        || promotion != null && mv.getPieceAt(to).getPieceShape() == promotion)
                        )
                        .collect(Collectors.toList())
        );
    }

    public boolean isShortCastling() {
        return isCastling() && to.getX() == 6;
    }

    public boolean isLongCastling() {
        return isCastling() && to.getX() == 2;
    }

    public String toFen() {
        String piecePlacement = resultPosition.toFen();
        String activeColor = getColorOfWhoToMove() == ChessmanColor.WHITE ? "w" : "b";
        String castlingAvailability = getCastlingAvailability();
        String enPassantTargetSquare = getEnPassantTargetSquare();

        StringBuilder sb = new StringBuilder();
        return sb.append(piecePlacement).append(" ")
        .append(activeColor).append(" ")
        .append(castlingAvailability).append(" ")
        .append(enPassantTargetSquare).append(" ")
        .append(halfmoveClock).append(" ")
        .append(fullmoveNumber).toString();
    }

    public boolean wasPawnPromotion() {
        return getPrevMove() != null
                && getPrevMove().getPieceAt(getFrom()).getPieceShape() == PieceShape.PAWN
                && getPieceAt(getTo()).getPieceShape() != PieceShape.PAWN;
    }

    public String getEnPassantTargetSquare() {
        if (prevMove != null) {
            if (prevMove.isChessmanOnCell(PieceShape.PAWN, from) && 2 == Math.abs(from.getY() - to.getY())) {
                int dy = getColorOfWhoMadeMove() == ChessmanColor.WHITE ? -1 : 1;
                return coordsToString(to.plusY(dy));
            } else {
                return "-";
            }
        } else {
            return enPassantTargetSquare;
        }
    }

    private Move makeMove(MoveNotationParts mnp) {
        ChessmanColor colorToMove = getColorOfWhoMadeMove().invert();
        Set<CellCoords> availableCellsFrom = findAll(c ->
                c.getPieceShape() == mnp.getPieceShape() && c.getPieceColor() == colorToMove
        );
        availableCellsFrom.removeIf(c ->
                mnp.getCoordFromX() != null && c.getX() != mnp.getCoordFromX()
                        || mnp.getCoordFromY() != null && c.getY() != mnp.getCoordFromY()
        );
        if (availableCellsFrom.isEmpty()) {
            throw new ParseMoveException("'" + mnp.getNotation() + "' - cannot find specified piece to move.");
        }
        List<Pair<CellCoords, List<Move>>> possibleMoves = availableCellsFrom.stream()
                .map(c -> Pair.of(c, this.getPossibleNextMoves(c)))
                .map(p -> Pair.of(p, p.getRight().stream().map(Move::getTo).collect(Collectors.toSet())))
                .filter(p -> p.getRight().contains(mnp.getCellToMoveTo()))
                .map(pp -> pp.getLeft())
                .collect(Collectors.toList());
        if (possibleMoves.isEmpty()) {
            throw new ParseMoveException("'" + mnp.getNotation() + "' - move is not possible.");
        }
        if (possibleMoves.size() > 1) {
            throw new ParseMoveException("Move is ambiguously specified.");
        }
        List<Move> result = possibleMoves.get(0).getRight().stream()
                .filter(m -> m.getTo().equals(mnp.getCellToMoveTo()))
                .collect(Collectors.toList());
        if (mnp.getPieceShape() == PieceShape.PAWN
                && (mnp.getCellToMoveTo().getY() == 0 || mnp.getCellToMoveTo().getY() == 7)) {
            if (mnp.getReplacement() == null) {
                throw new ParseMoveException("'" + mnp.getNotation() + "' - replacement is not specified.");
            }
            result.removeIf(
                    m->m.resultPosition.getPieceAt(mnp.getCellToMoveTo()).getPieceShape() != mnp.getReplacement()
            );
        }
        if (result.size() != 1) {
            throw new OutlineException("result.size() != 1");
        }
        return result.get(0);
    }

    private String getCastlingAvailability() {
        StringBuilder sb = new StringBuilder();
        if (whiteKingCastleAvailable) {
            sb.append("K");
        }
        if (whiteQueenCastleAvailable) {
            sb.append("Q");
        }
        if (blackKingCastleAvailable) {
            sb.append("k");
        }
        if (blackQueenCastleAvailable) {
            sb.append("q");
        }
        if (sb.length() == 0) {
            return "-";
        } else {
            return sb.toString();
        }
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

    private boolean isCastlingPossible(ChessmanType castlingType) {
        if (castlingType.getPieceColor() == ChessmanColor.WHITE) {
            Set<CellCoords> cellsUnderAttack = getAllCellsAttackedBy(ChessmanColor.BLACK);
            if (castlingType == WHITE_QUEEN) {
                return whiteQueenCastleAvailable
                        && isChessmanOnCell(WHITE_ROOK, A1)
                        && isChessmanOnCell((ChessmanType) null, B1)
                        && isChessmanOnCell((ChessmanType) null, C1)
                        && isChessmanOnCell((ChessmanType) null, D1)
                        && isChessmanOnCell(WHITE_KING, E1)
                        && !cellsUnderAttack.contains(D1)
                        && !cellsUnderAttack.contains(C1)
                        && !cellsUnderAttack.contains(E1);
            } else if (castlingType == WHITE_KING) {
                return whiteKingCastleAvailable
                        && isChessmanOnCell(WHITE_KING, E1)
                        && isChessmanOnCell((ChessmanType) null, F1)
                        && isChessmanOnCell((ChessmanType) null, G1)
                        && isChessmanOnCell(WHITE_ROOK, H1)
                        && !cellsUnderAttack.contains(F1)
                        && !cellsUnderAttack.contains(G1)
                        && !cellsUnderAttack.contains(E1);
            } else {
                return false;
            }
        } else {
            Set<CellCoords> cellsUnderAttack = getAllCellsAttackedBy(ChessmanColor.WHITE);
            if (castlingType == BLACK_QUEEN) {
                return blackQueenCastleAvailable
                        && isChessmanOnCell(BLACK_ROOK, A8)
                        && isChessmanOnCell((ChessmanType) null, B8)
                        && isChessmanOnCell((ChessmanType) null, C8)
                        && isChessmanOnCell((ChessmanType) null, D8)
                        && isChessmanOnCell(BLACK_KING, E8)
                        && !cellsUnderAttack.contains(C8)
                        && !cellsUnderAttack.contains(D8)
                        && !cellsUnderAttack.contains(E8);
            } else if (castlingType == BLACK_KING) {
                return blackKingCastleAvailable
                        && isChessmanOnCell(BLACK_KING, E8)
                        && isChessmanOnCell((ChessmanType) null, F8)
                        && isChessmanOnCell((ChessmanType) null, G8)
                        && isChessmanOnCell(BLACK_ROOK, H8)
                        && !cellsUnderAttack.contains(F8)
                        && !cellsUnderAttack.contains(G8)
                        && !cellsUnderAttack.contains(E8);
            } else {
                return false;
            }
        }
    }

    private Set<CellCoords> getPossibleCastlings(ChessmanType selectedChessman) {
        Set<CellCoords> result = new HashSet<>();
        if (selectedChessman.equals(WHITE_KING)) {
            if (isCastlingPossible(WHITE_QUEEN)) {
                result.add(C1);
            }
            if (isCastlingPossible(WHITE_KING)) {
                result.add(G1);
            }
        } else if (selectedChessman.equals(BLACK_KING)) {
            if (isCastlingPossible(BLACK_QUEEN)) {
                result.add(C8);
            }
            if (isCastlingPossible(BLACK_KING)) {
                result.add(G8);
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
                && lastMovedPiece == BLACK_PAWN
                && selectedCell.getY() == 4
                && from.getY() == 6 && to.getY() == 4
        ) {
            if (selectedCell.plusX(-1).equals(to)) {
                return setOf(selectedCell.plusXY(-1, 1));
            } else if (selectedCell.plusX(1).equals(to)) {
                return setOf(selectedCell.plusXY(1, 1));
            }
        } else if (selectedPiece == BLACK_PAWN
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
        if (isChessmanOnCell(chessboardAfterMove, WHITE_KING, to)) {
            if (from.equals(E1) && to.equals(C1)) {
                chessboardAfterMove.placePiece(D1, chessboardAfterMove.getPieceAt(A1));
                chessboardAfterMove.placePiece(A1, null);
            } else if (from.equals(E1) && to.equals(G1)) {
                chessboardAfterMove.placePiece(F1, chessboardAfterMove.getPieceAt(H1));
                chessboardAfterMove.placePiece(H1, null);
            }
        }
        if (isChessmanOnCell(chessboardAfterMove, BLACK_KING, to)) {
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
        newMove.whiteQueenCastleAvailable = (prevMove == null || prevMove.whiteQueenCastleAvailable)
                        && isWhiteARookOnInitialCell(newMove)
                        && isWhiteKingOnInitialCell(newMove);

        newMove.whiteKingCastleAvailable = (prevMove == null || prevMove.whiteKingCastleAvailable)
                        && isWhiteHRookOnInitialCell(newMove)
                        && isWhiteKingOnInitialCell(newMove);

        newMove.blackQueenCastleAvailable = (prevMove == null || prevMove.blackQueenCastleAvailable)
                        && isBlackARookOnInitialCell(newMove)
                        && isBlackKingOnInitialCell(newMove);

        newMove.blackKingCastleAvailable = (prevMove == null || prevMove.blackKingCastleAvailable)
                        && isBlackHRookOnInitialCell(newMove)
                        && isBlackKingOnInitialCell(newMove);
    }

    private boolean isWhiteARookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, WHITE_ROOK, A1);
    }

    private boolean isWhiteHRookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, WHITE_ROOK, H1);
    }

    private boolean isWhiteKingOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, WHITE_KING, E1);
    }

    private boolean isBlackARookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, BLACK_ROOK, A8);
    }

    private boolean isBlackHRookOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, BLACK_ROOK, H8);
    }

    private boolean isBlackKingOnInitialCell(Move move) {
        return isChessmanOnCell(move.resultPosition, BLACK_KING, E8);
    }

    private boolean isChessmanOnCell(PieceShape pieceShape, CellCoords coords) {
        return isChessmanOnCell(resultPosition, pieceShape, coords);
    }

    private boolean isChessmanOnCell(ChessmanType type, CellCoords coords) {
        return isChessmanOnCell(resultPosition, type, coords);
    }

    private boolean isChessmanOnCell(ChessBoard chessBoard, ChessmanType type, CellCoords coords) {
        return type == chessBoard.getPieceAt(coords);
    }

    private boolean isChessmanOnCell(ChessBoard chessBoard, PieceShape pieceShape, CellCoords coords) {
        final ChessmanType chessmanAtCoords = chessBoard.getPieceAt(coords);
        return pieceShape==null && chessmanAtCoords==null
                || chessmanAtCoords!=null && chessmanAtCoords.getPieceShape() == pieceShape;
    }

    private static String generateShortNotation(Move move) {
        if (move.getFrom() == null) {
            return "start-position";
        } else if (move.isShortCastling()) {
            return "O-O";
        } else if (move.isLongCastling()) {
            return "O-O-O";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(whatMoved(move));
        if (wasCapture(move)) {
            sb.append("x");
        }
        sb.append(coordsToString(move.getTo()));
        if (move.wasPawnPromotion()) {
            sb.append("=").append(chessmanShapeToShortSymbol(move.getPieceAt(move.getTo())));
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

    private int calcHalfmoveClock(Move prevMove, CellCoords to, ChessBoard resultPosition) {
        if (
                (prevMove != null && prevMove.resultPosition.getPieceAt(from).getPieceShape() == PieceShape.PAWN)
                || resultPosition.getPieceAt(to).getPieceShape() == PieceShape.PAWN
                        || prevMove.getPieceAt(to) != null) {
            return 0;
        } else {
            return prevMove.halfmoveClock + 1;
        }
    }

    private int calcFullmoveNumber(Move prevMove, CellCoords to, ChessBoard resultPosition) {
        if (resultPosition.getPieceAt(to).getPieceColor() == ChessmanColor.BLACK) {
            return prevMove.fullmoveNumber + 1;
        } else {
            return prevMove.fullmoveNumber;
        }
    }

    private void setCastlingAvailabilities(String fen) {
        String castlings = fen.split("\\s")[2];
        whiteKingCastleAvailable = castlings.contains("K");
        whiteQueenCastleAvailable = castlings.contains("Q");
        blackKingCastleAvailable = castlings.contains("k");
        blackQueenCastleAvailable = castlings.contains("q");
    }

    private static ChessmanColor getColorToMove(String fen) {
        return "w".equals(fen.split("\\s")[1]) ? ChessmanColor.WHITE : ChessmanColor.BLACK;
    }

    private static String getEnPassantTargetSquare(String fen) {
        return fen.split("\\s")[3];
    }

    private static int getHalfmoveClock(String fen) {
        return Integer.parseInt(fen.split("\\s")[4]);
    }

    private static int getFullmoveClock(String fen) {
        return Integer.parseInt(fen.split("\\s")[5]);
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

    @Getter
    @Setter
    private static class MoveNotationParts {
        private String notation;
        private PieceShape pieceShape;
        private Integer coordFromX;
        private Integer coordFromY;
        private CellCoords cellToMoveTo;
        private PieceShape replacement;
    }
}
