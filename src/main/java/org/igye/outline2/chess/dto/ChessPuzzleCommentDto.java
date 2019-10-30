package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.dto.NodeDto;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChessPuzzleCommentDto {
    private UUID id;
    private String text;
}
