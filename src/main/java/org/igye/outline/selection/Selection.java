package org.igye.outline.selection;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Selection {
    private ActionType actionType;
    private List<SelectionPart> selections;
}
