package org.igye.outline.htmlforms;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.igye.outline.model.User;
import org.igye.outline.selection.Selection;

@Data
@NoArgsConstructor
public class SessionData {
    private User user;
    private Selection selection;
}
