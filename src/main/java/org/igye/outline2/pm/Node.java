package org.igye.outline2.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hibernate.annotations.CascadeType.DELETE;
import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REFRESH;
import static org.hibernate.annotations.CascadeType.REMOVE;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
public class Node {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    private Node parentNode;

    private String name;

    @OneToMany(mappedBy = "parentNode")
    @OrderColumn(name = "ord")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE, DELETE})
    private List<Node> childNodes = new ArrayList<>();

    @ManyToOne
    private Image icon;

    private Instant createdWhen = Instant.now();

    public void addChild(Node child) {
        Hibernate.initialize(childNodes);
        child.setParentNode(this);
        childNodes.add(child);
    }

    public void detachChild(Node child) {
        Hibernate.initialize(childNodes);
        child.setParentNode(null);
        childNodes.removeIf(c -> c.getId().equals(child.getId()));
    }

    public boolean isTopNode() {
        return getId() == null;
    }

    public List<Node> getPath() {
        List<Node> path = new ArrayList<>();
        Node curNode = this;
        while (curNode != null && !curNode.isTopNode()) {
            path.add(curNode);
            curNode = curNode.getParentNode();
        }
        Collections.reverse(path);
        return path;
    }
}
