package org.igye.outline2.chess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PositionDto {
    private String notation;
    private String fen;
    private String move;
    private PositionAnalysisDto analysis;
    private List<UUID> puzzleIds = new ArrayList<>();
}
