package org.igye.outline2.chess.manager.analyse;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.MoveAnalysisDto;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionAnalysisDto;
import org.igye.outline2.chess.dto.PositionDto;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.igye.outline2.common.OutlineUtils.contains;

public class PgnAnalyser {

    public static final Pattern MOVE_PATTERN = Pattern.compile("^([a-h][1-8]){2}[qrbn]?$");
    private static final String SCORE_OPTION = ScoreStockfishInfoOption.class.getSimpleName();
    private static final String DEPTH_OPTION = DepthStockfishInfoOption.class.getSimpleName();
    private static final String PV_OPTION = PvStockfishInfoOption.class.getSimpleName();

    public static ParsedPgnDto analysePgn(String runStockfishCmd, String pgn, Integer depth, Integer moveTimeSec) throws IOException {
        try (ConsoleAppRunner stockfish = new ConsoleAppRunner(runStockfishCmd)) {
            ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgn);

            stockfish.readTill(contains("Stockfish"));
            stockfish.send("uci");
            stockfish.readTill(contains("uciok"));
            stockfish.send("setoption name MultiPV value 5");
            stockfish.send("ucinewgame");

            for (List<PositionDto> fullMove : parsedPgnDto.getPositions()) {
                for (PositionDto halfMove : fullMove) {
                    halfMove.setAnalysis(analyseFen(stockfish, halfMove.getFen(), depth, moveTimeSec));
                }
            }

            return parsedPgnDto;
        }
    }

    protected static PositionAnalysisDto analyseFen(ConsoleAppRunner stockfish, String fen,
                                                    Integer depth, Integer moveTimeSec) throws IOException {
        stockfish.send("position fen " + fen);
        stockfish.send(
                "go"
                        + (depth != null?(" depth " + depth):"")
                        + (moveTimeSec != null?(" movetime " + moveTimeSec*1000):"")
        );
        return collectAnalysisData(stockfish, isWhite(fen));
    }

    protected static List<StockfishInfoOption> parseInfo(String info) {
        return parseLine(1, info.split("\\s+"));
    }

    private static PositionAnalysisDto collectAnalysisData(ConsoleAppRunner stockfish,
                                                           boolean isWhite) throws IOException {
        PositionAnalysisDto result = new PositionAnalysisDto();
        HashMap<String, MoveAnalysisDto> foundMoves = new HashMap<>();
        stockfish.read(line -> {
            if (line.startsWith("info ")) {
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
                    foundMoves.put(
                            move,
                            MoveAnalysisDto.builder()
                                    .move(move)
                                    .depth(depthOption.getValue())
                                    .score(scoreOption.getCp())
                                    .mate(scoreOption.getMate())
                                    .build()
                    );
                }
            } else if (line.startsWith("bestmove ")) {
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
                            .sorted((m1,m2) -> compareMoves(m1,m2,isWhite))
                            .collect(Collectors.toList())
            );
        } else {
            result.setPossibleMoves(Collections.emptyList());
        }
        return result;
    }

    protected static int compareMoves(MoveAnalysisDto m1, MoveAnalysisDto m2, boolean isWhite) {
        int result;
        if (m1.getMate() != null) {
            if (m2.getMate() != null) {
                double mate1 = 1.0 / m1.getMate();
                double mate2 = 1.0 / m2.getMate();
                result = mate1 >= mate2 ? -1 : 1;
            } else {
                result = m1.getMate() > 0 ? -1 : 1;
            }
        } else {
            if (m2.getMate() != null) {
                result = m2.getMate() > 0 ? 1 : -1;
            } else if (m1.getScore().equals(m2.getScore())) {
                result = 0;
            } else if (m1.getScore() > m2.getScore()) {
                result = -1;
            } else {
                result = 1;
            }
        }
        if (!isWhite) {
            result *= -1;
        }
        return result;
    }

    private static boolean isWhite(String fen) {
        return "w".equals(fen.split("\\s")[1]);
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
