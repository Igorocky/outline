package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessBoardView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.HistoryRow;
import org.igye.outline2.chess.manager.MovesBuilder;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.PieceShape;
import org.igye.outline2.common.Function3;
import org.junit.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.igye.outline2.common.OutlineUtils.map;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetter;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetterWithDefault;
import static org.igye.outline2.common.OutlineUtils.setOf;

public class ChessTestUtils {
    public static final String PREPARED_TO_MOVE_COLOR = "#FFFF00";
    private static final Predicate<ChessBoardCellView> CELL_PREPARED_TO_MOVE =
            cell -> PREPARED_TO_MOVE_COLOR.equals(cell.getBorderColor());
    public static final String AVAILABLE_TO_MOVE_TO_COLOR = "#90EE90";
    private static final Predicate<ChessBoardCellView> CELL_AVAILABLE_TO_MOVE_TO =
            cell -> AVAILABLE_TO_MOVE_TO_COLOR.equals(cell.getBorderColor());
    public static final String CHOOSE_CHESSMAN_TYPE_COLOR = "#0000AA";

    public static void assertCellPreparedToMove(ChessComponentView view, CellCoords expectedCellCoords) {
        assertSetsEqual(
                setOf(expectedCellCoords),
                map(findAll(view, CELL_PREPARED_TO_MOVE), ChessBoardCellView::getCoords)
        );
    }

    public static void assertNoCellPreparedToMove(ChessComponentView view) {
        Assert.assertTrue(findAll(view, CELL_PREPARED_TO_MOVE).isEmpty());
    }

    public static void assertCellsAvailableToMoveTo(ChessComponentView view, Set<CellCoords> expectedCellCoords) {
        assertSetsEqual(
                expectedCellCoords,
                map(findAll(view, CELL_AVAILABLE_TO_MOVE_TO), ChessBoardCellView::getCoords)
        );
    }

    public static <T> void assertSetsEqual(Set<T> expected, Set<T> actual) {
        if (!Objects.equals(expected, actual)) {
            HashSet missing = new HashSet(expected);
            missing.removeAll(actual);
            HashSet redundant = new HashSet(actual);
            redundant.removeAll(expected);
            String msg = "\nMissing  : " + missing
                       + "\nRedundant: " + redundant;
            Assert.fail(msg);
        }
    }

    public static void assertNoCellsAvailableToMoveTo(ChessComponentView view) {
        Assert.assertTrue(findAll(view, CELL_AVAILABLE_TO_MOVE_TO).isEmpty());
    }

    public static Move initialPosition(ChessmanColor whoToMove, Consumer<ChessBoardBuilder> chessBoardBuilderConsumer) {
        final ChessBoardBuilder chessBoardBuilder = chessBoardBuilder();
        chessBoardBuilderConsumer.accept(chessBoardBuilder);
        ChessBoard initialBoard = chessBoardBuilder.build();
        return new Move(initialBoard, whoToMove);
    }

    public static ChessBoard chessBoard(Consumer<ChessBoardBuilder> chessBoardBuilderConsumer) {
        final ChessBoardBuilder chessBoardBuilder = chessBoardBuilder();
        chessBoardBuilderConsumer.accept(chessBoardBuilder);
        return chessBoardBuilder.build();
    }

    public static ChessBoardView chessBoardView(Consumer<ChessBoardViewBuilder> chessBoardBuilderConsumer) {
        final ChessBoardViewBuilder chessBoardBuilder = new ChessBoardViewBuilder();
        chessBoardBuilderConsumer.accept(chessBoardBuilder);
        return chessBoardBuilder.build();
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
                final ChessBoardCellView cell = chessBoardView.getCell(x, y);
                if (cell != null && !consumer.apply(x, y, cell)) {
                    break;
                }
            }
        }
    }

    public static ChessBoardBuilder chessBoardBuilder() {
        return new ChessBoardBuilder();
    }

    public static void assertEqualsByChessmenTypes(ChessBoard expected, ChessBoard actual) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                String msg = "Comparing: x = " + x + " y = " + y
                        + " expected=" + expected.toFen() + " actual=" + actual.toFen();
                Assert.assertTrue(
                        msg,
                        Objects.equals(expected.getPieceAt(x,y), actual.getPieceAt(x,y))
                );
            }
        }

    }

    public static void assertEqualsByChessmenTypes(ChessBoard expected, ChessComponentView actual) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                final ChessBoardView actualChessBoard = actual.getChessBoard();
                if (!Objects.equals(
                        expected.getPieceAt(x, y),
                        nullSafeGetter(
                                actualChessBoard.getCell(x, y),
                                p -> p.getCode() == 0 ? null : ChessmanType.fromCode(p.getCode())
                        )
                )) {
                    int finalY = y;
                    String expectedLine = Stream.iterate(0, i -> i + 1).limit(8)
                            .map(xi -> expected.getPieceAt(xi, finalY))
                            .map(ChessTestUtils::chessmenToString)
                            .reduce("", (a, b) -> a + b);

                    String actualLine = Stream.iterate(0, i -> i + 1).limit(8)
                            .map(xi -> actual.getChessBoard().getCell(xi, finalY))
                            .map(ChessTestUtils::cellToString)
                            .reduce("", (a, b) -> a + b);

                    String msg = "\nExpected " + (y+1) + ": " + expectedLine
                               + "\nActual   " + (y+1) + ": " + actualLine;
                    Assert.fail(msg);
                }
            }
        }
    }

    public static boolean equals(ChessBoardCellView expected, ChessBoardCellView actual) {
        return Objects.equals(expected.getCoords(), actual.getCoords())
        && Objects.equals(expected.getBorderColor(), actual.getBorderColor())
        && expected.getCode() == actual.getCode();
    }

    public static void assertBoardsEqual(ChessBoardView expected, ChessBoardView actual) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (!equals(expected.getCell(x, y), actual.getCell(x, y))) {
                    int finalY = y;
                    String expectedLine = Stream.iterate(0, i -> i + 1).limit(8)
                            .map(xi -> expected.getCell(xi, finalY))
                            .map(ChessTestUtils::cellToString)
                            .reduce("", (a, b) -> a + b);

                    String actualLine = Stream.iterate(0, i -> i + 1).limit(8)
                            .map(xi -> actual.getCell(xi, finalY))
                            .map(ChessTestUtils::cellToString)
                            .reduce("", (a, b) -> a + b);

                    String msg = "\nExpected " + (y+1) + ": " + expectedLine
                               + "\nActual   " + (y+1) + ": " + actualLine;
                    Assert.fail(msg);
                }
            }
        }
    }

    private static String chessmenToString(ChessmanType chessman) {
        return nullSafeGetterWithDefault(
                chessman,
                ChessmanType::getPieceShape,
                PieceShape::getSymbol,
                s -> "  "+(chessman.getPieceColor() == ChessmanColor.WHITE ? s.toUpperCase() : s.toLowerCase()),
                "  ."
        );
    }

    private static String cellToString(ChessBoardCellView cell) {
        String type = nullSafeGetterWithDefault(
                cell,
                ChessBoardCellView::getCode,
                code -> code == 0 ? null : code,
                ChessmanType::fromCode,
                ChessmanType::getSymbol,
                '.'
        ).toString();

        String borderColor = nullSafeGetterWithDefault(
                cell,
                ChessBoardCellView::getBorderColor,
                color ->
                        color.equals(PREPARED_TO_MOVE_COLOR) ? "y" :
                        color.equals(AVAILABLE_TO_MOVE_TO_COLOR) ? "g" :
                        color.equals(CHOOSE_CHESSMAN_TYPE_COLOR) ? "b" :
                        "?",
                "u"
        );

        return " " + borderColor + type;
    }

    public static ChessComponentView execCommand(MovesBuilder movesBuilder, String command) {
        return movesBuilder.execChessCommand(command, null).getChessComponentView();
    }

    public static String getLastMove(ChessComponentView view) {
        final List<HistoryRow> moves = view.getHistory().getRows();
        HistoryRow lastMove = moves.get(moves.size() - 1);
        if (lastMove.getBlacksMove() != null) {
            return lastMove.getBlacksMove();
        } else {
            return lastMove.getWhitesMove();
        }
    }

    public static String getSelectedMove(ChessComponentView view) {
        for (HistoryRow historyRow : view.getHistory().getRows()) {
            if (historyRow.isWhitesMoveSelected()) {
                return historyRow.getWhitesMove();
            } else if (historyRow.isBlacksMoveSelected()) {
                return historyRow.getBlacksMove();
            }
        }
        return null;
    }
}
