package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.dto.PositionAnalysisDto;
import org.igye.outline2.chess.manager.analyse.FenAnalyser;
import org.igye.outline2.chess.manager.analyse.FenAnalyzerOptions;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.PieceShape;

import java.io.IOException;
import java.util.function.Consumer;

import static org.igye.outline2.chess.manager.ChessUtils.strCoordToInt;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetter;

public class StockFishRunner {

    public static Move getNextMove(String runStockfishCmd, Move currPosition, int depth,
                                   Consumer<Pair<Integer,Integer>> depthChangeCallback) throws IOException {
        try (FenAnalyser fenAnalyser = new FenAnalyser(runStockfishCmd)) {
            final Long[] maxDepth = {0L};
            PositionAnalysisDto analysisResults = fenAnalyser.analyseFen(
                    currPosition.toFen(),
                    FenAnalyzerOptions.builder().depth(depth).build(),
                    progressInfo -> {
                        if (progressInfo.getDepth() != null && progressInfo.getDepth() > maxDepth[0]) {
                            if (depthChangeCallback != null) {
                                depthChangeCallback.accept(Pair.of(Math.toIntExact(progressInfo.getDepth()), depth));
                            }
                            maxDepth[0] = progressInfo.getDepth();
                        }
                    }
            );
            String bestMoveStr = analysisResults.getPossibleMoves().get(0).getMove();
            CellCoords from = new CellCoords(
                    strCoordToInt(bestMoveStr.substring(0,1)),
                    strCoordToInt(bestMoveStr.substring(1,2))
            );
            CellCoords to = new CellCoords(
                    strCoordToInt(bestMoveStr.substring(2,3)),
                    strCoordToInt(bestMoveStr.substring(3,4))
            );
            PieceShape replacement = nullSafeGetter(
                    bestMoveStr.length() > 4 ? bestMoveStr.substring(4,5) : null,
                    s -> s.toUpperCase(),
                    PieceShape::fromSymbol
            );
            return currPosition.makeMove(from, to, replacement);
        }
    }
}
