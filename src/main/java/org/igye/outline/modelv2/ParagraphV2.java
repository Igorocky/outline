package org.igye.outline.modelv2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REFRESH;
import static org.hibernate.annotations.CascadeType.REMOVE;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;

@Getter
@Setter
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
