package org.igye.outline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

import static org.hibernate.annotations.CascadeType.*;
import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Data
@NoArgsConstructor
@Entity
public class Paragraph {
    public static final String ROOT_NAME = "root";

    @Id
    @GeneratedValue
    @Type(type = UUID_CHAR)
    private UUID id;

    @ManyToOne
    private User owner;

    @NotNull
    private String name;

    @ManyToOne
    @JoinColumn(name = "PARENTPARAGRAPH_ID")
    private Paragraph parentParagraph;

    @OneToMany(mappedBy = "parentParagraph")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn(name = "CHILDPARAGRAPHS_ORDER")
    private List<Paragraph> childParagraphs = new ArrayList<>();

    @OneToMany(mappedBy = "paragraph")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<Topic> topics = new ArrayList<>();

    @ManyToMany
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE})
    @JoinTable(name = "PARAGRAPH_TAG")
    private Set<Tag> tags = new HashSet<>();

    public void addChildParagraph(Paragraph paragraph) {
        getChildParagraphs().add(paragraph);
        paragraph.setParentParagraph(this);
        paragraph.setOwner(getOwner());
    }

    public void addTopic(Topic topic) {
        getTopics().add(topic);
        topic.setParagraph(this);
        topic.setOwner(getOwner());
    }

    public boolean getHasChildren() {
        return !getChildParagraphs().isEmpty() || !getTopics().isEmpty();
    }

    public boolean getHasParent() {
        return getParentParagraph() != null;
    }
}
