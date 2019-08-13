package org.igye.outline2.chess.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailablePiecesListDto {
    private List<List<ChessBoardCellDto>> availablePieces;
}
