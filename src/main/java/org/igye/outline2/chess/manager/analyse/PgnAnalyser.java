package org.igye.outline2.chess.manager.analyse;

import org.igye.outline2.chess.dto.MoveAnalysisDto;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionAnalysisDto;
import org.igye.outline2.chess.dto.PositionDto;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class PgnAnalyser {
    public static final long MAX_DELTA = 9999;

    public static ParsedPgnDto analysePgn(String runStockfishCmd, String pgn,
                                          Integer depth, Integer moveTimeSec, int numberOfProcesses,
                                          Consumer<PgnAnalysisProgressInfo> progressCallback) throws IOException {
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgn);
        PgnAnalysisProgressInfo progressInfo = PgnAnalysisProgressInfo.builder()
                .halfMovesToAnalyse(
                        parsedPgnDto.getPositions().stream().map(List::size).reduce(0, (a,b)->a+b)
                )
                .currHalfMove(0)
                .build();
        PositionAnalysisDto prevAnalysisResults = null;

        try (FenAnalyser fenAnalyser = new FenAnalyser(runStockfishCmd)) {
            for (List<PositionDto> fullMove : parsedPgnDto.getPositions()) {
                for (PositionDto halfMove : fullMove) {
                    if (progressCallback != null) {
                        progressInfo = progressInfo.withCurrHalfMove(progressInfo.getCurrHalfMove()+1);
                        progressCallback.accept(progressInfo);
                    }
                    final PositionAnalysisDto analysisResults = fenAnalyser.analyseFen(
                            halfMove.getFen(), depth, moveTimeSec, null
                    );
                    analysisResults.setDelta(calcDelta(
                            prevAnalysisResults, analysisResults, isBlackToMove(halfMove.getFen())
                    ));
                    halfMove.setAnalysis(analysisResults);
                    prevAnalysisResults = analysisResults;
                }
            }

            return parsedPgnDto;
        }
    }

    protected static Long calcDelta(PositionAnalysisDto prevAnalysisResults,
                                       PositionAnalysisDto curAnalysisResults,
                                       boolean isBlackToMove) {
        if (prevAnalysisResults == null) {
            return null;
        }
        MoveAnalysisDto prevBestMove = getBestMove(prevAnalysisResults);
        MoveAnalysisDto curBestMove = getBestMove(curAnalysisResults);
        if (prevBestMove != null && curBestMove != null) {
            if (prevBestMove.getMate() != null && curBestMove.getMate() == null) {
                if (prevBestMove.getMate() > 0) {
                    return -MAX_DELTA * (isBlackToMove?1:-1);
                } else {
                    return MAX_DELTA * (isBlackToMove?1:-1);
                }
            }
            if (prevBestMove.getMate() == null && curBestMove.getMate() != null) {
                if (curBestMove.getMate() > 0) {
                    return MAX_DELTA * (isBlackToMove?1:-1);
                } else {
                    return -MAX_DELTA * (isBlackToMove?1:-1);
                }
            }

            if (prevBestMove.getScore() != null && curBestMove.getScore() != null) {
                return (curBestMove.getScore() - prevBestMove.getScore()) * (isBlackToMove?1:-1);
            }
        }
        return null;
    }

    private static MoveAnalysisDto getBestMove(PositionAnalysisDto analysisResults) {
        if (!analysisResults.getPossibleMoves().isEmpty()) {
            return analysisResults.getPossibleMoves().get(0);
        } else {
            return null;
        }
    }

    private static boolean isBlackToMove(String fen) {
        return "b".equals(fen.split("\\s")[1]);
    }
}
