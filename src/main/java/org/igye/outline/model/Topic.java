package org.igye.outline.model;

import org.hibernate.annotations.Cascade;
import org.igye.outline.typeconverters.ListConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hibernate.annotations.CascadeType.*;

@Entity
public class Topic {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User owner;

    @NotNull
    private String name;

    @NotNull
    @ManyToOne
    private Paragraph paragraph;

    @Convert(converter = ListConverter.class)
    private List<String> images = new ArrayList<>();

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

    public Paragraph getParagraph() {
        return paragraph;
    }

    public void setParagraph(Paragraph paragraph) {
        this.paragraph = paragraph;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
