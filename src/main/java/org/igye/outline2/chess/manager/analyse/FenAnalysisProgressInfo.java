package org.igye.outline2.chess.manager.analyse;

import com.fasterxml.jackson.annotation.JsonFormat;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Wither;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Immutable
@Wither
public class FenAnalysisProgressInfo {
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssZ", timezone="UTC")
    private Instant lastUpdated;
}
