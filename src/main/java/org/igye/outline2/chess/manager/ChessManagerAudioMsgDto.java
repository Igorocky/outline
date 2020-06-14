package org.igye.outline2.chess.manager;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChessManagerAudioMsgDto implements ChessManagerAudioDto {
    private String msg;
}
