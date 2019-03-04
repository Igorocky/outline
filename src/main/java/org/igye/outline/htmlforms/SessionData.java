package org.igye.outline.htmlforms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline.config.UserDetailsImpl;
import org.igye.outline.data.NodeDao;
import org.igye.outline.model.User;
import org.igye.outline.selection.Selection;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor
public class SessionData {
    @Getter @Setter
    private Selection selection;
    private User user;
    @Getter @Setter
    private LearnTextData learnTextData = new LearnTextData();
    @Getter @Setter
    private CyclicRandom cyclicRandom = new CyclicRandom();
    @Getter @Setter
    private LearnNodesData learnNodesData;

    public SessionData(NodeDao nodeDao) {
        learnNodesData = new LearnNodesData(nodeDao);
    }

    public synchronized User getCurrentUser() {
        if (user == null) {
            user = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        }
        return user;
    }
}
