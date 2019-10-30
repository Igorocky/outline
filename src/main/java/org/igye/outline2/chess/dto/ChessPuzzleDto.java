package org.igye.outline2.chess.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.dto.NodeDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChessPuzzleDto extends NodeDto {
    private List<ChessPuzzleCommentDto> comments = new ArrayList<>();
    private List<ChessPuzzleAttemptDto> attempts = new ArrayList<>();
}
