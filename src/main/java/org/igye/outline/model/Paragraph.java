package org.igye.outline.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REFRESH;
import static org.hibernate.annotations.CascadeType.REMOVE;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;
import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
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
