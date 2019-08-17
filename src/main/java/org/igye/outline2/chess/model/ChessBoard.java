package org.igye.outline2.chess.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.manager.ChessUtils;
import org.igye.outline2.common.Function3;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ChessBoard {
    private List<List<ChessmanType>> board;

    public ChessBoard() {
        clear();
    }

    public ChessBoard(String str) {
        clear();
        if (!StringUtils.isEmpty(str)) {
            decode(str);
        }
    }

    public List<List<ChessmanType>> getBoard() {
        return board;
    }

    public void placePiece(CellCoords coords, ChessmanType chessman) {
        placePiece(coords.getX(), coords.getY(), chessman);
    }

    public void placePiece(int x, int y, ChessmanType chessman) {
        board.get(x).set(y, chessman);
    }

    public ChessmanType getPieceAt(CellCoords coords) {
        return getPieceAt(coords.getX(), coords.getY());
    }

    public ChessmanType getPieceAt(int x, int y) {
        return board.get(x).get(y);
    }

    public Set<CellCoords> getPossibleMoves(CellCoords from) {
        ChessmanType chessman = getPieceAt(from);
        if (chessman.getPieceShape() == PieceShape.PAWN) {
            return getPossibleTargetCellsForPawn(chessman.getPieceColor(), from);
        } else if (chessman.getPieceShape() == PieceShape.KNIGHT) {
            return getPossibleTargetCellsForKnight(chessman.getPieceColor(), from);
        } else if (chessman.getPieceShape() == PieceShape.BISHOP) {
            return getPossibleTargetCellsForBishop(chessman.getPieceColor(), from);
        } else if (chessman.getPieceShape() == PieceShape.ROOK) {
            return getPossibleTargetCellsForRook(chessman.getPieceColor(), from);
        } else if (chessman.getPieceShape() == PieceShape.QUEEN) {
            return getPossibleTargetCellsForQueen(chessman.getPieceColor(), from);
        } else if (chessman.getPieceShape() == PieceShape.KING) {
            return getPossibleTargetCellsForKing(chessman.getPieceColor(), from);
        } else {
            return Collections.emptySet();
        }
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        final int height = board.get(0).size();
        final int width = board.size();
        int emptyCellCnt = 0;
        for (int y = height-1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                ChessmanType piece = getPieceAt(x, y);
                if (piece == null) {
                    emptyCellCnt++;
                } else {
                    writeEmptyCellCnt(sb, emptyCellCnt);
                    emptyCellCnt = 0;
                    sb.append(chessmanToString(board.get(x).get(y)));
                }
            }
        }
        if (emptyCellCnt > 0) {
            writeEmptyCellCnt(sb, emptyCellCnt);
        }
        return sb.toString();
    }

    public CellCoords findFirstCoords(Predicate<ChessmanType> predicate) {
        final CellCoords[] result = {null};
        traverse((x,y,chessman) -> {
            if (predicate.test(chessman)) {
                result[0] = new CellCoords(x,y);
                return false;
            }
            return true;
        });
        return result[0];
    }

    public void traverse(Function3<Integer, Integer, ChessmanType, Boolean> consumer) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                final ChessmanType chessman = getPieceAt(x, y);
                if (chessman != null && !consumer.apply(x, y, chessman)) {
                    break;
                }
            }
        }
    }

    public ChessBoard clone() {
        return new ChessBoard(encode());
    }

    private void decode(String encodedPosition) {
        int cellPointer = 0;
        int charPointer = 0;
        while (cellPointer < 64) {
            cellPointer = processNextChar(encodedPosition.charAt(charPointer), cellPointer);
            charPointer++;
        }
    }

    private int processNextChar(char ch, int cellPointer) {
        if (isPiece(ch)) {
            int x = cellPointer%8;
            int y = 7-cellPointer/8;
            placePiece(x,y, ChessmanType.fromSymbol(ch));
            return cellPointer + 1;
        } else {
            return cellPointer + ch - 48;
        }
    }

    private boolean isPiece(char ch) {
        return 'p' == ch || 'P' == ch || 'r' == ch || 'n' == ch || 'b' == ch || 'q' == ch || 'k' == ch
                || 'R' == ch || 'N' == ch || 'B' == ch || 'Q' == ch || 'K' == ch;
    }

    private void clear() {
        board = ChessUtils.emptyBoard(8,8, (x,y)->null);
    }

    private void writeEmptyCellCnt(StringBuilder sb, int cnt) {
        if (cnt == 0) {
            return;
        }
        int cnt9 = cnt / 9;
        int rem = cnt%9;
        while (cnt9 > 0) {
            sb.append("9");
            cnt9--;
        }
        if (rem > 0) {
            sb.append(rem + "");
        }
    }

    private String chessmanToString(ChessmanType chessman) {
        if (chessman.getPieceColor().equals(ChessmanColor.WHITE)) {
            return chessman.getPieceShape().getSymbol().toString().toUpperCase();
        } else {
            return chessman.getPieceShape().getSymbol().toString();
        }
    }

    private Set<CellCoords> getPossibleTargetCellsForPawn(ChessmanColor colorOfWhoIsMoving, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        if (colorOfWhoIsMoving.equals(ChessmanColor.WHITE)) {
            CellCoords left = from.plusX(-1).plusY(1);
            if (isInsideBoard(left) && isEnemy(colorOfWhoIsMoving, left)) {
                result.add(left);
            }
            CellCoords right = from.plusX(1).plusY(1);
            if (isInsideBoard(right) && isEnemy(colorOfWhoIsMoving, right)) {
                result.add(right);
            }
            CellCoords forward = from.plusX(0).plusY(1);
            if (getPieceAt(forward) == null) {
                result.add(forward);
            }
            CellCoords forward2 = from.plusX(0).plusY(2);
            if (from.getY() == 1 && getPieceAt(forward2) == null) {
                result.add(forward2);
            }
        } else {
            CellCoords left = from.plusX(-1).plusY(-1);
            if (isInsideBoard(left) && isEnemy(colorOfWhoIsMoving, left)) {
                result.add(left);
            }
            CellCoords right = from.plusX(1).plusY(-1);
            if (isInsideBoard(right) && isEnemy(colorOfWhoIsMoving, right)) {
                result.add(right);
            }
            CellCoords forward = from.plusX(0).plusY(-1);
            if (getPieceAt(forward) == null) {
                result.add(forward);
            }
            CellCoords forward2 = from.plusX(0).plusY(-2);
            if (from.getY() == 6 && getPieceAt(forward2) == null) {
                result.add(forward2);
            }
        }
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForKnight(ChessmanColor colorOfWhoIsMoving, CellCoords from) {
        return Arrays.asList(
                Pair.of(-2, 1),
                Pair.of(-2, -1),
                Pair.of(2, 1),
                Pair.of(2, -1),
                Pair.of(-1, 2),
                Pair.of(+1, 2),
                Pair.of(-1, -2),
                Pair.of(+1, -2)
        ).stream()
                .map(pair -> from.plusX(pair.getLeft()).plusY(pair.getRight()))
                .filter(dest -> canBePlacedTo(colorOfWhoIsMoving, dest))
                .collect(Collectors.toSet());
    }

    private Set<CellCoords> getPossibleTargetCellsForBishop(ChessmanColor colorOfWhoIsMoving, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(createRay(colorOfWhoIsMoving, from, 1,1));
        result.addAll(createRay(colorOfWhoIsMoving, from, 1,-1));
        result.addAll(createRay(colorOfWhoIsMoving, from, -1,-1));
        result.addAll(createRay(colorOfWhoIsMoving, from, -1,1));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForRook(ChessmanColor colorOfWhoIsMoving, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(createRay(colorOfWhoIsMoving, from, 0,1));
        result.addAll(createRay(colorOfWhoIsMoving, from, 0,-1));
        result.addAll(createRay(colorOfWhoIsMoving, from, 1,0));
        result.addAll(createRay(colorOfWhoIsMoving, from, -1,0));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForQueen(ChessmanColor colorOfWhoIsMoving, CellCoords from) {
        Set<CellCoords> result = new HashSet<>();
        result.addAll(getPossibleTargetCellsForBishop(colorOfWhoIsMoving, from));
        result.addAll(getPossibleTargetCellsForRook(colorOfWhoIsMoving, from));
        return result;
    }

    private Set<CellCoords> getPossibleTargetCellsForKing(ChessmanColor colorOfWhoIsMoving, CellCoords from) {
        return Arrays.asList(
                Pair.of(-1, 1),
                Pair.of(-1, 0),
                Pair.of(-1, -1),
                Pair.of(0, 1),
                Pair.of(0, -1),
                Pair.of(1, 1),
                Pair.of(1, 0),
                Pair.of(1, -1)
        ).stream()
                .map(pair -> from.plusX(pair.getLeft()).plusY(pair.getRight()))
                .filter(dest -> canBePlacedTo(colorOfWhoIsMoving, dest))
                .collect(Collectors.toSet());
    }

    private Set<CellCoords> createRay(ChessmanColor colorOfWhoIsMoving, CellCoords from, int dx, int dy) {
        Set<CellCoords> result = new HashSet<>();
        CellCoords to = from.plusX(dx).plusY(dy);
        while (canBePlacedTo(colorOfWhoIsMoving, to)) {
            result.add(to);
            if (isEnemy(colorOfWhoIsMoving, to)) {
                break;
            }
            to = to.plusX(dx).plusY(dy);
        }
        return result;
    }

    private boolean isInsideBoard(CellCoords coords) {
        return 0 <= coords.getX() && coords.getX() <= 7 && 0 <= coords.getY() && coords.getY() <= 7;
    }

    private boolean canBePlacedTo(ChessmanColor colorOfWhoIsMoving, CellCoords to) {
        if (!isInsideBoard(to)) {
            return false;
        }
        ChessmanType chessmanTypeAtDestination = getPieceAt(to);
        if (chessmanTypeAtDestination == null) {
            return true;
        }
        if (!chessmanTypeAtDestination.getPieceColor().equals(colorOfWhoIsMoving)) {
            return true;
        }
        return false;
    }

    private boolean isEnemy(ChessmanColor colorOfWhoIsMoving, CellCoords at) {
        if (!isInsideBoard(at)) {
            return false;
        }
        ChessmanType chessmanTypeAtDestination = getPieceAt(at);
        return chessmanTypeAtDestination != null
                && !chessmanTypeAtDestination.getPieceColor().equals(colorOfWhoIsMoving);
    }
}
