package org.igye.outline.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Paragraph {
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
    @OrderColumn
    private List<Paragraph> childParagraphs;

    @OneToMany(mappedBy = "paragraph")
    @OrderColumn
    private List<Topic> topics = new ArrayList<>();

    @ManyToMany
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

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
