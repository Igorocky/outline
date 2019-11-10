package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
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
import org.igye.outline2.exceptions.OutlineException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StockfishAnalyser {

    public static final Pattern MOVE_PATTERN = Pattern.compile("^([a-h][1-8]){2}$");

    protected static List<StockfishInfoOption> parseInfo(String info) {
        return parseLine(1, info.split("\\s+"));
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
                idx+=(score.getCp() != null || score.getMate() != null)?3:2;
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
        if ("cp".equals(args[idx])) {
            return ScoreStockfishInfoOption.builder().cp(parseLong(args[idx+1])).build();
        } else if ("mate".equals(args[idx])) {
            return ScoreStockfishInfoOption.builder().mate(parseLong(args[idx+1])).build();
        } else if ("lowerbound".equals(args[idx])) {
            return ScoreStockfishInfoOption.builder().lowerbound(true).build();
        } else if ("upperbound".equals(args[idx])) {
            return ScoreStockfishInfoOption.builder().upperbound(true).build();
        } else {
            throw new OutlineException("Unrecognized score format: " + StringUtils.join(args, " "));
        }
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
