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
@Audited
public class Node implements HasTags {
    @Id
    private UUID id = UUID.randomUUID();
    private Instant createdWhen;

    private String clazz = NodeClasses.CONTAINER;

    @OneToMany(mappedBy = "node", orphanRemoval = true)
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE, DELETE})
    private List<Tag> tags = new ArrayList<>();

    @ManyToOne
    private Node parentNode;

    @OneToMany(mappedBy = "parentNode")
    @OrderColumn(name = "ord")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE, DELETE})
    private List<Node> childNodes = new ArrayList<>();

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

    public void addTag(Tag tag) {
        Hibernate.initialize(tags);
        tag.setNode(this);
        tags.add(tag);
    }

    public void detachTag(Tag tag) {
        Hibernate.initialize(tags);
        tag.setNode(null);
        tags.removeIf(c -> c.getId().equals(tag.getId()));
    }

    public boolean isTopNode() {
        return getClazz().equals(NodeClasses.TOP_CONTAINER);
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
