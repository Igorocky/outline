package org.igye.outline2.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.envers.Audited;
import org.igye.outline2.OutlineUtils;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
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
public class Node {
    @Id
    private UUID id = UUID.randomUUID();
    private Instant createdWhen;

    @ManyToOne
    private Node parentNode;

    @ElementCollection
    @CollectionTable(name = "tag")
    private List<Tag> tags = new ArrayList<>();

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

    public void setTagValues(UUID tagId, List<String> tagValues) {
        removeTagValues(tagId);
        tagValues.forEach(value -> tags.add(Tag.builder().tagId(tagId).value(value).build()));
    }

    public void addTagValue(UUID tagId, String value) {
        Hibernate.initialize(tags);
        tags.add(Tag.builder().tagId(tagId).value(value).build());
    }

    public List<String> getTagValues(UUID tagId) {
        Hibernate.initialize(tags);
        List<String> result = new ArrayList<>();
        for (Tag tag : tags) {
            if (tagId.equals(tag.getTagId())) {
                result.add(tag.getValue());
            }
        }
        return result;
    }

    public String getTagSingleValue(UUID tagId) {
        return OutlineUtils.getSingleValue(getTagValues(tagId));
    }

    public void setTagSingleValue(UUID tagId, String value) {
        removeTagValues(tagId);
        tags.add(Tag.builder().tagId(tagId).value(value).build());
    }

    public void removeTagValues(UUID tagId) {
        Hibernate.initialize(tags);
        tags.removeIf(tag -> tag.getTagId().equals(tagId));
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
