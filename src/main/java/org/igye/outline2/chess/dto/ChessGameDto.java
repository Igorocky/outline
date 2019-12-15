package org.igye.outline2.chess.dto;

import lombok.Getter;
import lombok.Setter;
import org.igye.outline2.dto.NodeDto;

@Getter
@Setter
public class ChessGameDto extends NodeDto {
    private ParsedPgnDto parsedPgn;
    private int analysisInitDepth;
    private int analysisInitNumberOfThreads;
}
