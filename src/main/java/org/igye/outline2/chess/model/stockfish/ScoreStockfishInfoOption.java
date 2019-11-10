package org.igye.outline2.chess.model.stockfish;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Immutable
@Builder
@AllArgsConstructor
public class ScoreStockfishInfoOption implements StockfishInfoOption {
    private Long cp;
    private Long mate;
    private Boolean lowerbound;
    private Boolean upperbound;
}
