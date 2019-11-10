package org.igye.outline2.chess.model.stockfish;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Immutable
@Builder
@AllArgsConstructor
public class RefutationStockfishInfoOption implements StockfishInfoOption {
    private List<String> moves;
}
