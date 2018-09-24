package org.igye.outline.modelv2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        Hibernate.initialize(getChildNodes());
        getChildNodes().add(node);
        node.setParentNode(this);
        node.setOwner(getOwner());
    }

    public void removeChildNodeById(UUID id) {
        Hibernate.initialize(getChildNodes());
        int i = 0;
        List<NodeV2> childNodes = getChildNodes();
        while (i < childNodes.size()) {
            if (childNodes.get(i).getId().equals(id)) {
                childNodes.get(i).setParentNode(null);
                childNodes.remove(i);
            } else {
                i++;
            }
        }
    }

    public boolean getHasChildren() {
        return !getChildNodes().isEmpty();
    }

    public boolean getHasParent() {
        return getParentNode() != null;
    }
}
