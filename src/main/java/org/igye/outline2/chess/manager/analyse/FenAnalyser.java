package org.igye.outline2.chess.manager.analyse;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.MoveAnalysisDto;
import org.igye.outline2.chess.dto.PositionAnalysisDto;
import org.igye.outline2.chess.model.stockfish.CpuloadStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.CurrlineStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.CurrmoveStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.CurrmovenumberStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.DepthStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.HashfullStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.MultipvStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.NodesStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.NpsStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.PvStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.RefutationStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.SbhitsStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.ScoreStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.SelDepthStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.StockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.StringStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.TbhitsStockfishInfoOption;
import org.igye.outline2.chess.model.stockfish.TimeStockfishInfoOption;
import org.igye.outline2.common.ConsoleAppRunner;
import org.igye.outline2.exceptions.OutlineException;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.igye.outline2.chess.manager.StockFishRunner.BEST_MOVE_ANS_PATTERN;
import static org.igye.outline2.common.OutlineUtils.contains;
import static org.igye.outline2.common.OutlineUtils.toJson;

public class FenAnalyser implements Closeable {

    public static final Pattern MOVE_PATTERN = Pattern.compile("^([a-h][1-8]){2}[qrbn]?$");
    private static final String SCORE_OPTION = ScoreStockfishInfoOption.class.getSimpleName();
    private static final String DEPTH_OPTION = DepthStockfishInfoOption.class.getSimpleName();
    private static final String PV_OPTION = PvStockfishInfoOption.class.getSimpleName();

    private ConsoleAppRunner stockfish;

    public FenAnalyser(String runStockfishCmd) throws IOException {
        stockfish = new ConsoleAppRunner(runStockfishCmd);
        stockfish.readTill(contains("Stockfish"));
        stockfish.send("uci");
        stockfish.readTill(contains("uciok"));
        stockfish.send("setoption name MultiPV value 5");
        stockfish.send("setoption name UCI_AnalyseMode value true");
    }

    public synchronized PositionAnalysisDto analyseFen(String fen, Integer depth, Integer moveTimeSec,
                                          Consumer<FenAnalysisProgressInfo> progressCallback) throws IOException {
        stockfish.send("ucinewgame");
        stockfish.send("position fen " + fen);
        stockfish.send(
                "go"
                        + (depth != null?(" depth " + depth):"")
                        + (moveTimeSec != null?(" movetime " + moveTimeSec*1000):"")
        );
        return collectAnalysisData(isBlackToMove(fen), progressCallback);
    }

    @Override
    public void close() {
        stockfish.close();
    }

    private PositionAnalysisDto collectAnalysisData(boolean isBlackToMove,
                                                    Consumer<FenAnalysisProgressInfo> progressCallback
    ) throws IOException {
        PositionAnalysisDto result = new PositionAnalysisDto();
        HashMap<String, MoveAnalysisDto> foundMoves = new HashMap<>();
        final String[] bestMove = new String[1];
        FenAnalysisProgressInfo progressInfo = FenAnalysisProgressInfo.builder()
                .lastUpdated(Instant.now())
                .build();
        stockfish.read(line -> {
            if (line.startsWith("info ")) {
                MoveAnalysisDto moveInfo = parseMoveInfo(line);
                if (moveInfo != null) {
                    foundMoves.put(moveInfo.getMove(), moveInfo);
                    if (progressCallback != null) {
                        progressCallback.accept(progressInfo.withLastUpdated(Instant.now()));
                    }
                }
            } else if (line.startsWith("bestmove ")) {
                Matcher bestMoveMatcher = BEST_MOVE_ANS_PATTERN.matcher(line);
                if (bestMoveMatcher.find()) {
                    bestMove[0] = bestMoveMatcher.group(1);
                }
                return false;
            }
            return true;
        });
        final ArrayList<MoveAnalysisDto> possibleMoves = new ArrayList<>(foundMoves.values());

        if (!possibleMoves.isEmpty()) {
            Long maxDepth = possibleMoves.stream().map(MoveAnalysisDto::getDepth).max(Long::compareTo).get();
            result.setPossibleMoves(
                    possibleMoves.stream()
                            .filter(mv -> mv.getDepth() == maxDepth)
                            .sorted(FenAnalyser::compareMoves)
                            .collect(Collectors.toList())
            );
        } else {
            result.setPossibleMoves(Collections.emptyList());
        }
        checkBestMove(bestMove[0], result.getPossibleMoves());
        if (isBlackToMove) {
            for (MoveAnalysisDto possibleMove : result.getPossibleMoves()) {
                if (possibleMove.getMate() != null) {
                    possibleMove.setMate(-possibleMove.getMate());
                }
                if (possibleMove.getScore() != null) {
                    possibleMove.setScore(-possibleMove.getScore());
                }
            }
        }
        return result;
    }

    private MoveAnalysisDto parseMoveInfo(String line) {
        Map<String, List<StockfishInfoOption>> options = parseInfo(line).stream()
                .filter(opt -> (opt instanceof ScoreStockfishInfoOption)
                        || (opt instanceof PvStockfishInfoOption)
                        || (opt instanceof DepthStockfishInfoOption)
                )
                .collect(Collectors.groupingBy(opt -> opt.getClass().getSimpleName()));
        if (options.containsKey(SCORE_OPTION) && options.containsKey(PV_OPTION)) {
            for (String key : options.keySet()) {
                if (options.get(key).size() != 1) {
                    throw new OutlineException("options.get(" + key + ").size() == " + options.get(key).size());
                }
            }
            ScoreStockfishInfoOption scoreOption = (ScoreStockfishInfoOption) options.get(SCORE_OPTION).get(0);
            PvStockfishInfoOption pvOption = (PvStockfishInfoOption) options.get(PV_OPTION).get(0);
            DepthStockfishInfoOption depthOption = (DepthStockfishInfoOption) options.get(DEPTH_OPTION).get(0);
            final String move = pvOption.getMoves().get(0);
            return MoveAnalysisDto.builder()
                    .move(move)
                    .depth(depthOption.getValue())
                    .score(scoreOption.getCp())
                    .mate(scoreOption.getMate())
                    .build();
        } else {
            return null;
        }
    }

    protected static List<StockfishInfoOption> parseInfo(String info) {
        return parseLine(1, info.split("\\s+"));
    }

    protected void checkBestMove(String bestMove, List<MoveAnalysisDto> possibleMoves) {
        if (bestMove == null) {
            if (possibleMoves.isEmpty()) {
                return;
            } else {
                throw new OutlineException("bestMove == null && !possibleMoves.isEmpty(): possibleMoves = "
                        + toJson(possibleMoves));
            }
        }
        if (bestMove != null) {
            if (possibleMoves.isEmpty()) {
                throw new OutlineException("bestMove != null && possibleMoves.isEmpty(): bestMove = " + bestMove);
            }
            if (bestMove.equals(possibleMoves.get(0).getMove())) {
                return;
            }
            Set<String> bestMoves;
            if (possibleMoves.get(0).getMate() != null) {
                Long bestMate = possibleMoves.get(0).getMate();
                bestMoves = possibleMoves.stream()
                        .filter(mv -> mv.getMate() != null)
                        .filter(mv -> mv.getMate().equals(bestMate))
                        .map(MoveAnalysisDto::getMove)
                        .collect(Collectors.toSet());
            } else {
                Long bestScore = possibleMoves.get(0).getScore();
                bestMoves = possibleMoves.stream()
                        .filter(mv -> mv.getScore() != null)
                        .filter(mv -> mv.getScore().equals(bestScore))
                        .map(MoveAnalysisDto::getMove)
                        .collect(Collectors.toSet());
            }
            if (!bestMoves.contains(bestMove)) {
                throw new OutlineException(
                        "!bestMoves.contains(bestMove): bestMove = " + bestMove
                                + ", possibleMoves = " + toJson(possibleMoves)
                );
            }
        }
    }

    protected static int compareMoves(MoveAnalysisDto m1, MoveAnalysisDto m2) {
        if (m1.getMate() != null) {
            if (m2.getMate() != null) {
                if (m1.getMate().equals(m2.getMate())) {
                    return 0;
                } else if (m1.getMate() > 0 && m2.getMate() < 0) {
                    return -1;
                } else if (m1.getMate() < 0 && m2.getMate() > 0) {
                    return 1;
                } else {
                    return m1.getMate() < m2.getMate() ? -1 : 1;
                }
            } else {
                return m1.getMate() > 0 ? -1 : 1;
            }
        } else {
            if (m2.getMate() != null) {
                return m2.getMate() > 0 ? 1 : -1;
            } else if (m1.getScore().equals(m2.getScore())) {
                return  0;
            } else {
                return m1.getScore() > m2.getScore() ? -1 : 1;
            }
        }
    }

    private boolean isBlackToMove(String fen) {
        return "b".equals(fen.split("\\s")[1]);
    }

    private static List<StockfishInfoOption> parseLine(int idx, String[] args) {
        List<StockfishInfoOption> options = new ArrayList<>();
        while (idx < args.length) {
            if (StringUtils.isBlank(args[idx])) {
                idx+=1;
            } else if ("depth".equals(args[idx])) {
                options.add(DepthStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("seldepth".equals(args[idx])) {
                options.add(SelDepthStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("time".equals(args[idx])) {
                options.add(TimeStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("nodes".equals(args[idx])) {
                options.add(NodesStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("pv".equals(args[idx])) {
                final PvStockfishInfoOption option = PvStockfishInfoOption.builder().moves(readMoves(idx + 1, args)).build();
                options.add(option);
                idx+=1+option.getMoves().size();
            } else if ("multipv".equals(args[idx])) {
                options.add(MultipvStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("score".equals(args[idx])) {
                final ScoreStockfishInfoOption score = readScore(idx + 1, args);
                options.add(score);
                int deltaIdx = 1;
                deltaIdx += (score.getCp() != null || score.getMate() != null)?2:0;
                deltaIdx += (score.getLowerbound() != null || score.getUpperbound() != null)?1:0;
                idx+=deltaIdx;
            } else if ("currmove".equals(args[idx])) {
                options.add(CurrmoveStockfishInfoOption.builder().value(args[idx+1]).build());
                idx+=2;
            } else if ("currmovenumber".equals(args[idx])) {
                options.add(CurrmovenumberStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("hashfull".equals(args[idx])) {
                options.add(HashfullStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("nps".equals(args[idx])) {
                options.add(NpsStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("tbhits".equals(args[idx])) {
                options.add(TbhitsStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("sbhits".equals(args[idx])) {
                options.add(SbhitsStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("cpuload".equals(args[idx])) {
                options.add(CpuloadStockfishInfoOption.builder().value(parseLong(args[idx+1])).build());
                idx+=2;
            } else if ("string".equals(args[idx])) {
                options.add(StringStockfishInfoOption.builder().value(args[idx+1]).build());
                idx=args.length;
            } else if ("refutation".equals(args[idx])) {
                final RefutationStockfishInfoOption refutation =
                        RefutationStockfishInfoOption.builder().moves(readMoves(idx + 1, args)).build();
                options.add(refutation);
                idx+=1+refutation.getMoves().size();
            } else if ("currline".equals(args[idx])) {
                final CurrlineStockfishInfoOption option = readCurrline(idx + 1, args);
                options.add(option);
                idx+=1+(option.getCpuNum()==null?0:1)+option.getMoves().size();
            } else {
                throw new OutlineException(
                        "Unrecognized uci option " + args[idx] + " at " + idx
                                + " in line " + StringUtils.join(args, " ")
                );
            }
        }
        return options;
    }

    private static ScoreStockfishInfoOption readScore(int idx, String[] args) {
        Long cp = null;
        Long mate = null;
        Boolean lowerbound = null;
        Boolean upperbound = null;
        if ("cp".equals(args[idx])) {
            cp = parseLong(args[idx+1]);
            if ("lowerbound".equals(args[idx+2])) {
                lowerbound = true;
            } else if ("upperbound".equals(args[idx+2])) {
                upperbound = true;
            }
        } else if ("mate".equals(args[idx])) {
            mate = parseLong(args[idx+1]);
        } else if ("lowerbound".equals(args[idx])) {
            lowerbound = true;
        } else if ("upperbound".equals(args[idx])) {
            upperbound = true;
        } else {
            throw new OutlineException("Unrecognized score format: " + StringUtils.join(args, " "));
        }
        return ScoreStockfishInfoOption.builder()
            .cp(cp)
            .mate(mate)
            .lowerbound(lowerbound)
            .upperbound(upperbound)
            .build();
    }

    private static CurrlineStockfishInfoOption readCurrline(int idx, String[] args) {
        Long cpuNum = null;
        final Matcher matcher = MOVE_PATTERN.matcher(args[idx]);
        if (!matcher.matches()) {
            cpuNum = parseLong(args[idx]);
        }
        return CurrlineStockfishInfoOption.builder()
                .cpuNum(cpuNum)
                .moves(readMoves(cpuNum == null ? idx : idx+1, args))
                .build();
    }

    private static List<String> readMoves(int idx, String[] args) {
        List<String> moves = new ArrayList<>();
        int mi = idx;
        while (mi < args.length) {
            Matcher matcher = MOVE_PATTERN.matcher(args[mi]);
            if (!matcher.matches()) {
                break;
            }
            moves.add(matcher.group(0));
            mi++;
        }

        return moves ;
    }

    private static long parseLong(String str) {
        return Long.parseLong(str);
    }
}
