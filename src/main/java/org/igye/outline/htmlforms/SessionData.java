package org.igye.outline.htmlforms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline.config.UserDetailsImpl;
import org.igye.outline.modelv2.UserV2;
import org.igye.outline.selection.Selection;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor
public class SessionData {
    @Getter
    @Setter
    private Selection selection;
    private UserV2 user;

    public UserV2 getCurrentUser() {
        if (user == null) {
            user = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        }
        return user;
    }

}
