package org.igye.outline2.chess.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionsToFindNextMoves {
    @Builder.Default
    private boolean checkColor = true;
    @Builder.Default
    private boolean checkPossibleCastlings = true;
    @Builder.Default
    private boolean performSelfCheckValidation = true;
}
