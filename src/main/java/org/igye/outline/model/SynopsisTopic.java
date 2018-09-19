package org.igye.outline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.annotations.CascadeType.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "SYNOPSISTOPIC")
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

    public void detachContentById(Content content) {
        int idx = 0;
        while (idx < getContents().size()) {
            if (getContents().get(idx).getId().equals(content.getId())) {
                getContents().remove(idx);
            } else {
                idx++;
            }
        }
        content.setTopic(null);
    }

}
