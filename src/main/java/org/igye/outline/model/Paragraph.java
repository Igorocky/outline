package org.igye.outline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
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

@Data
@NoArgsConstructor
@Entity
public class Paragraph extends Node {
    public static final String ROOT_NAME = "root";

    @OneToMany(mappedBy = "parentNode")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<Node> childNodes = new ArrayList<>();

    public void addChildNode(Node node) {
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
