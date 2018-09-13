package org.igye.outline.modelv2;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.*;

import static org.hibernate.annotations.CascadeType.*;

@Data
@NoArgsConstructor
@Entity
public class ParagraphV2 extends NodeV2 {
    @OneToMany(mappedBy = "parentNode")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<NodeV2> childNodes = new ArrayList<>();

    public void addChildNode(NodeV2 node) {
        getChildNodes().add(node);
        node.setParentNode(this);
        node.setOwner(getOwner());
    }

    public boolean getHasChildren() {
        return !getChildNodes().isEmpty();
    }

    public boolean getHasParent() {
        return getParentNode() != null;
    }
}
