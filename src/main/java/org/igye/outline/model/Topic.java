package org.igye.outline.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
public class Topic extends Node {
    @OneToMany(mappedBy = "topic")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<Content> contents = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
    private Icon icon;

    private boolean eol;

    public void addContent(Content content) {
        Hibernate.initialize(getContents());
        getContents().add(content);
        content.setTopic(this);
        content.setOwner(getOwner());
    }

    public void detachContentById(Content content) {
        Hibernate.initialize(getContents());
        int idx = 0;
        while (idx < getContents().size()) {
            if (getContents().get(idx).getId().equals(content.getId())) {
                content.setTopic(null);
                getContents().remove(idx);
            } else {
                idx++;
            }
        }
        content.setTopic(null);
    }

    public boolean getHasParent() {
        return getParentNode() != null;
    }
}
