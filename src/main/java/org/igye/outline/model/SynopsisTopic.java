package org.igye.outline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.annotations.CascadeType.*;
import static org.hibernate.annotations.CascadeType.REMOVE;

@Data
@NoArgsConstructor
@Entity
public class SynopsisTopic extends Topic {
    @OneToMany(mappedBy = "topic")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<Content> contents = new ArrayList<>();

    public void addContent(Content content) {
        getContents().add(content);
        content.setTopic(this);
        content.setOwner(getOwner());
    }

    public void detachContent(Content content) {
        getContents().remove(content);
        content.setTopic(null);
    }

}
