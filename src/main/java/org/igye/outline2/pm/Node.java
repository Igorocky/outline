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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    private NodeClass clazz;
    private Instant createdWhen;

    @ElementCollection
    @CollectionTable(name = "tag")
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

    public void setValueTags(TagId tagId, List<String> tagValues) {
        removeTags(tagId);
        tagValues.forEach(tagValue -> this.tags.add(Tag.builder().tagId(tagId).value(tagValue).build()));
    }

    public void setTags(TagId tagId, List<Tag> tagValues) {
        removeTags(tagId);
        tagValues.forEach(tagValue -> {
            tagValue.setTagId(tagId);
            this.tags.add(tagValue);
        });
    }

    public void addTag(TagId tagId, String tagValue) {
        Hibernate.initialize(tags);
        tags.add(Tag.builder().tagId(tagId).value(tagValue).build());
    }

    public List<Tag> getTagValues(UUID tagId) {
        Hibernate.initialize(tags);
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags) {
            if (tagId.equals(tag.getTagId())) {
                result.add(tag);
            }
        }
        return result;
    }

    public String getTagSingleValue(UUID tagId) {
        return OutlineUtils.getSingleValue(getTagValues(tagId)).getValue();
    }

    public void setTagSingleValue(TagId tagId, String value) {
        removeTags(tagId);
        if (value != null) {
            tags.add(Tag.builder()
                    .tagId(tagId)
                    .value(value)
                    .build()
            );
        }
    }

    public void setTagSingleValue(TagId tagId, Node ref) {
        removeTags(tagId);
        if (ref != null) {
            tags.add(Tag.builder()
                    .tagId(tagId)
                    .ref(ref)
                    .build()
            );
        }
    }

    public void removeTags(TagId tagId) {
        Hibernate.initialize(tags);
        tags.removeIf(tag -> tag.getTagId().equals(tagId));
    }

    public boolean isTopNode() {
        return getClazz().equals(NodeClass.TOP_CONTAINER);
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
