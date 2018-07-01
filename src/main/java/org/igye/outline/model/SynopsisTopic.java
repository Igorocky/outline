package org.igye.outline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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
    private List<Content> contents;
}
