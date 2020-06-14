package org.igye.outline2.chess.manager;

import lombok.Builder;
import lombok.Data;
import org.igye.outline2.chess.dto.ChessmenPositionQuizCard;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChessManagerAudioStateDto implements ChessManagerAudioDto {
    private UUID puzzleId;
    private List<ChessmenPositionQuizCard> startPosition;
}
