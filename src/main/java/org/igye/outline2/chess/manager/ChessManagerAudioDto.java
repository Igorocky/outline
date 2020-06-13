package org.igye.outline2.chess.manager;

import lombok.Builder;
import lombok.Data;
import org.igye.outline2.chess.dto.ChessmenPositionQuizCard;

import java.util.List;

@Data
@Builder
public class ChessManagerAudioDto {
    private List<ChessmenPositionQuizCard> startPosition;
}
