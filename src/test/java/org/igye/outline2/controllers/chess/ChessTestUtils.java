package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessBoardView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.common.Function3;
import org.junit.Assert;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.igye.outline2.OutlineUtils.setOf;

public class ChessTestUtils {
    private static final Predicate<ChessBoardCellView> CELL_PREPARED_TO_MOVE =
            cell -> "yellow".equals(cell.getBorderColor());
    private static final Predicate<ChessBoardCellView> CELL_AVAILABLE_TO_MOVE_TO =
            cell -> "green".equals(cell.getBorderColor());

    public static void assertCellPreparedToMove(ChessComponentView view, CellCoords expectedCellCoords) {
        Assert.assertEquals(
                map(findAll(view, CELL_PREPARED_TO_MOVE), ChessBoardCellView::getCoords),
                setOf(expectedCellCoords)
        );
    }

    public static void assertNoCellPreparedToMove(ChessComponentView view) {
        Assert.assertTrue(findAll(view, CELL_PREPARED_TO_MOVE).isEmpty());
    }

    public static void assertCellsAvailableToMoveTo(ChessComponentView view, Set<CellCoords> expectedCellCoords) {
        Assert.assertEquals(
                map(findAll(view, CELL_AVAILABLE_TO_MOVE_TO), ChessBoardCellView::getCoords),
                expectedCellCoords
        );
    }

    public static void assertNoCellsAvailableToMoveTo(ChessComponentView view) {
        Assert.assertTrue(findAll(view, CELL_AVAILABLE_TO_MOVE_TO).isEmpty());
    }

    public static Move initialPosition(ChessmanColor whoToMove, Consumer<ChessBoardBuilder> chessBoardBuilderConsumer) {
        final ChessBoardBuilder chessBoardBuilder = chessBoardBuilder();
        chessBoardBuilderConsumer.accept(chessBoardBuilder);
        ChessBoard initialBoard = chessBoardBuilder.build();
        ChessmanColor colorOfWhoMadePreviousMove = whoToMove.inverse();
        return Move.builder()
                .to(initialBoard.findFirstCoords(cm -> cm.getType().getPieceColor().equals(colorOfWhoMadePreviousMove)))
                .resultPosition(initialBoard)
                .build();
    }

    public static Set<ChessBoardCellView> findAll(ChessComponentView chessComponentView,
                                                  Predicate<ChessBoardCellView> predicate) {
        Set<ChessBoardCellView> result = new HashSet<>();
        traverse(chessComponentView.getChessBoard(), (x,y,cell) -> {
            if (predicate.test(cell)) {
                result.add(cell);
            }
            return true;
        });
        return result;
    }

    public static void traverse(ChessBoardView chessBoardView,
                                Function3<Integer, Integer, ChessBoardCellView, Boolean> consumer) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                final ChessBoardCellView cell = chessBoardView.getCells().get(x).get(y);
                if (cell != null && !consumer.apply(x, y, cell)) {
                    break;
                }
            }
        }
    }

    public static ChessBoardBuilder chessBoardBuilder() {
        return new ChessBoardBuilder();
    }

    public static void assertEqualsByChessmenTypes(ChessBoard b1, ChessBoard b2) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                String msg = "Comparing: x = " + x + " y = " + y + " b1=" + b1.encode() + " b2=" + b2.encode();
                Assert.assertTrue(
                        msg,
                        Objects.equals(
                                nullSafeGetter(b1.getPieceAt(x,y), p->p.getType()),
                                nullSafeGetter(b2.getPieceAt(x,y), p->p.getType())
                        )
                );
            }
        }

    }
}
