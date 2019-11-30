package org.igye.outline2.chess.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParsedPgnDto {
    private String initialPositionFen;
    private String wPlayer;
    private String bPlayer;
    private List<List<PositionDto>> positions;
    private String analysisInfo;
}
