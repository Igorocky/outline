package org.igye.outline2.chess.manager.analyse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FenAnalyzerOptions {
    private Integer multiPV;
    private Integer depth;
    private Integer moveTimeSec;
}
