package org.igye.outline2.chess.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParsedPgnDto {
    private String wPlayer;
    private String bPlayer;
    private List<List<MoveDto>> moves;
}
