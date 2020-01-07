package org.igye.outline2.chess.manager.analyse;

import org.igye.outline2.chess.dto.MoveAnalysisDto;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionAnalysisDto;
import org.igye.outline2.chess.dto.PositionDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.manager.DtoConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class PgnAnalyser {
    public static final long MAX_DELTA = 9999;

    public static ParsedPgnDto analysePgn(String runStockfishCmd, String pgn,
                                          Integer depth, Integer moveTimeSec, int numberOfProcesses,
                                          DtoConverter dtoConverter,
                                          Consumer<PgnAnalysisProgressInfo> progressCallback) {
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgn);
        Map<String, PositionAnalysisDto> analysisResults = analyseMovesInParallel(
                parsedPgnDto, runStockfishCmd, depth, moveTimeSec, numberOfProcesses,
                wrapProgressCallback(parsedPgnDto, dtoConverter, progressCallback)
        );
        enrichPgnWithAnalysisInfo(parsedPgnDto, analysisResults);
        return parsedPgnDto;
    }

    private static Consumer<PgnAnalysisProgressInfo> wrapProgressCallback(
            ParsedPgnDto parsedPgnDto,
            DtoConverter dtoConverter,
            Consumer<PgnAnalysisProgressInfo> progressCallback) {
        final long[] lastDtoSendTime = {System.currentTimeMillis()};
        if (progressCallback == null) {
            return null;
        } else {
            return analysisProgressInfo -> {
                if (System.currentTimeMillis() - lastDtoSendTime[0] > 10000) {
                    lastDtoSendTime[0] = System.currentTimeMillis();
                    enrichPgnWithAnalysisInfo(parsedPgnDto, analysisProgressInfo.getAnalysisResults());
                    dtoConverter.enrich(parsedPgnDto);
                    progressCallback.accept(
                            analysisProgressInfo
                                    .withParsedPgn(parsedPgnDto)
                                    .withAnalysisResults(null)
                    );
                } else {
                    progressCallback.accept(
                            analysisProgressInfo
                                    .withAnalysisResults(null)
                    );
                }
            };
        }
    }

    private static void enrichPgnWithAnalysisInfo(ParsedPgnDto parsedPgnDto,
                                                  Map<String, PositionAnalysisDto> analysisResults) {
        for (List<PositionDto> fullMove : parsedPgnDto.getPositions()) {
            for (PositionDto halfMove : fullMove) {
                halfMove.setAnalysis(analysisResults.get(halfMove.getFen()));
            }
        }
        calcDeltas(parsedPgnDto);
    }

    private static Map<String, PositionAnalysisDto> analyseMovesInParallel(ParsedPgnDto parsedPgnDto,
                                                                           String runStockfishCmd,
                                                                           Integer depth,
                                                                           Integer moveTimeSec,
                                                                           int numberOfProcesses,
                                                                           Consumer<PgnAnalysisProgressInfo> progressCallback) {
        Map<String, PositionAnalysisDto> result = new ConcurrentHashMap<>();
        CountDownLatch waitAllThreadsCompleted = new CountDownLatch(numberOfProcesses);
        ConcurrentLinkedQueue<String> fensToAnalyse = new ConcurrentLinkedQueue<>();
        for (List<PositionDto> fullMove : parsedPgnDto.getPositions()) {
            for (PositionDto halfMove : fullMove) {
                fensToAnalyse.add(halfMove.getFen());
            }
        }
        AtomicReference<PgnAnalysisProgressInfo> pgnProgressInfo = new AtomicReference(
                PgnAnalysisProgressInfo.builder()
                        .halfMovesToAnalyse(fensToAnalyse.size())
                        .currHalfMove(0)
                        .procNumber(0)
                        .analysisResults(result)
                        .threadsInfo(new TreeMap<>())
                        .build()
        );
        AtomicInteger actualProcNumber = new AtomicInteger(0);
        for (int i = 0; i < numberOfProcesses; i++) {
            new Thread(() -> {
                try (FenAnalyser fenAnalyser = new FenAnalyser(runStockfishCmd)) {
                    updateProgressInfo(
                            progressCallback,
                            pgnProgressInfo,
                            progress -> {
                                Map<String,FenAnalysisProgressInfo> ti = new TreeMap<>(progress.getThreadsInfo());
                                ti.put(Thread.currentThread().getName(), null);
                                // TODO: 07.01.2020 this function should be side effect free,
                                //  but it does actualProcNumber.addAndGet
                                return progress
                                        .withProcNumber(actualProcNumber.addAndGet(1))
                                        .withThreadsInfo(ti);
                            }

                    );
                    String fenToAnalyse = fensToAnalyse.poll();
                    while (fenToAnalyse != null) {
                        result.put(
                                fenToAnalyse,
                                fenAnalyser.analyseFen(
                                        fenToAnalyse,
                                        depth,
                                        moveTimeSec,
                                        fenProgress -> updateProgressInfo(
                                                progressCallback,
                                                pgnProgressInfo,
                                                pgnProgress -> {
                                                    Map<String,FenAnalysisProgressInfo> ti =
                                                            new TreeMap<>(pgnProgress.getThreadsInfo());
                                                    ti.put(Thread.currentThread().getName(), fenProgress);
                                                    return pgnProgress.withThreadsInfo(ti);
                                                }

                                        )
                                )
                        );
                        updateProgressInfo(
                                progressCallback,
                                pgnProgressInfo,
                                progress -> progress
                                                .withCurrHalfMove(progress.getCurrHalfMove()+1)
                                                .withProcNumber(actualProcNumber.get())

                        );
                        fenToAnalyse = fensToAnalyse.poll();
                    }
                } catch (IOException e) {
                    throw new OutlineException(e);
                } finally {
                    waitAllThreadsCompleted.countDown();
                    updateProgressInfo(
                            progressCallback,
                            pgnProgressInfo,
                            progress -> {
                                Map<String,FenAnalysisProgressInfo> ti = new TreeMap<>(progress.getThreadsInfo());
                                ti.remove(Thread.currentThread().getName());
                                return progress
                                        .withProcNumber(actualProcNumber.addAndGet(-1))
                                        .withThreadsInfo(ti);
                            }

                    );
                }
            }).start();
        }
        try {
            waitAllThreadsCompleted.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OutlineException(e);
        }
        return result;
    }

    private static void updateProgressInfo(Consumer<PgnAnalysisProgressInfo> progressCallback,
                                           AtomicReference<PgnAnalysisProgressInfo> progressInfo,
                                           UnaryOperator<PgnAnalysisProgressInfo> unaryOperator) {
        if (progressCallback != null) {
            progressCallback.accept(progressInfo.updateAndGet(unaryOperator));
        }
    }

    private static void calcDeltas(ParsedPgnDto parsedPgnDto) {
        PositionAnalysisDto prevAnalysisResults = null;
        for (List<PositionDto> fullMove : parsedPgnDto.getPositions()) {
            for (PositionDto halfMove : fullMove) {
                final PositionAnalysisDto currAnalysisResults = halfMove.getAnalysis();
                if (currAnalysisResults != null) {
                    currAnalysisResults.setDelta(calcDelta(
                            prevAnalysisResults, currAnalysisResults, isBlackToMove(halfMove.getFen())
                    ));
                }
                prevAnalysisResults = currAnalysisResults;
            }
        }
    }

    protected static Long calcDelta(PositionAnalysisDto prevAnalysisResults,
                                       PositionAnalysisDto curAnalysisResults,
                                       boolean isBlackToMove) {
        if (prevAnalysisResults == null || curAnalysisResults == null) {
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
