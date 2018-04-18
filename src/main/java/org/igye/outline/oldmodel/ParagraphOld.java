package org.igye.outline.oldmodel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PARAGRAPHS")
public class ParagraphOld {
    @Id
    @GeneratedValue
    @Column(name = "\"id\"")
    private Long id;

    @Column(name = "\"name\"")
    private String name;

    @ManyToOne
    @JoinColumn(name = "\"paragraphId\"")
    private ParagraphOld parent;

    @OneToMany
    @OrderColumn(name = "\"order\"")
    @JoinColumn(name = "\"paragraphId\"")
    private List<ParagraphOld> childParagraphs;

    @OneToMany(mappedBy = "paragraph")
    @OrderColumn(name = "\"order\"")
    private List<TopicOld> topics = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParagraphOld getParent() {
        return parent;
    }

    public void setParent(ParagraphOld parent) {
        this.parent = parent;
    }

    public List<TopicOld> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicOld> topics) {
        this.topics = topics;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ParagraphOld> getChildParagraphs() {
        return childParagraphs;
    }

    public void setChildParagraphs(List<ParagraphOld> childParagraphs) {
        this.childParagraphs = childParagraphs;
    }

    @Override
    public String toString() {
        return "ParagraphOld{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parent=" + parent +
                '}';
    }
}
