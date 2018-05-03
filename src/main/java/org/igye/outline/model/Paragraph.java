package org.igye.outline.model;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hibernate.annotations.CascadeType.*;

@Entity
public class Paragraph {
    public static final String ROOT_NAME = "root";

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User owner;

    @NotNull
    private String name;

    @ManyToOne
    private Paragraph parentParagraph;

    @OneToMany(mappedBy = "parentParagraph")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<Paragraph> childParagraphs = new ArrayList<>();

    @OneToMany(mappedBy = "paragraph")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    @OrderColumn
    private List<Topic> topics = new ArrayList<>();

    @ManyToMany
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE})
    private Set<Tag> tags = new HashSet<>();

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Paragraph getParentParagraph() {
        return parentParagraph;
    }

    public void setParentParagraph(Paragraph parentParagraph) {
        this.parentParagraph = parentParagraph;
    }

    public List<Paragraph> getChildParagraphs() {
        return childParagraphs;
    }

    public void setChildParagraphs(List<Paragraph> childParagraphs) {
        this.childParagraphs = childParagraphs;
    }

    public void addChildParagraph(Paragraph paragraph) {
        getChildParagraphs().add(paragraph);
        paragraph.setParentParagraph(this);
        paragraph.setOwner(getOwner());
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public void addTopic(Topic topic) {
        getTopics().add(topic);
        topic.setParagraph(this);
        topic.setOwner(getOwner());
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
