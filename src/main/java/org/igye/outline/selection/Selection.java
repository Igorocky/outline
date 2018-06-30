package org.igye.outline.selection;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Selection {
    private SelectionAct selectionAct;
    private List<SelectionPart> selections;
}
