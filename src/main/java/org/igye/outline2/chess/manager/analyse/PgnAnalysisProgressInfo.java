package org.igye.outline2.chess.manager.analyse;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Immutable
@Wither
public class PgnAnalysisProgressInfo {
    private int halfMovesToAnalyse;
    private int currHalfMove;
    private int procNumber;
    private Map<String,FenAnalysisProgressInfo> threadsInfo;
}
