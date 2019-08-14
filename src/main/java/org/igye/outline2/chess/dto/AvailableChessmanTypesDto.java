package org.igye.outline2.chess.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailableChessmanTypesDto {
    private List<List<ChessBoardCellDto>> availableChessmanTypes;
}
