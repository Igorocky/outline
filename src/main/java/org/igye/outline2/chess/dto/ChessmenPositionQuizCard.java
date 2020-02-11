package org.igye.outline2.chess.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChessmenPositionQuizCard {
    private String question;
    private List<String> answer;
}
