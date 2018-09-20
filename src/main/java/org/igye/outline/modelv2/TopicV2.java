package org.igye.outline.modelv2;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.annotations.CascadeType.*;

@Data
@NoArgsConstructor
@Entity
public class TopicV2 extends NodeV2 {
    @OneToMany(mappedBy = "topic")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<ContentV2> contents = new ArrayList<>();

    public void addContent(ContentV2 content) {
        getContents().add(content);
        content.setTopic(this);
        content.setOwner(getOwner());
    }

    public void detachContentById(ContentV2 content) {
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