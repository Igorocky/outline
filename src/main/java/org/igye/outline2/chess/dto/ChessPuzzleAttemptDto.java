package org.igye.outline2.chess.dto;

import lombok.Getter;
import lombok.Setter;
import org.igye.outline2.dto.NodeDto;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
public class ChessPuzzleAttemptDto extends NodeDto {
    private Instant timestamp;
    private boolean passed;
    private Duration pauseDelay;
}
