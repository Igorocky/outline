package org.igye.outline.oldmodel;

import org.igye.outline.typeconverters.ListConverter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TOPICS")
public class TopicOld {
    @Id
    @GeneratedValue
    @Column(name = "\"id\"")
    private Long id;

    @ManyToOne
    @JoinColumn(name="\"paragraphId\"")
    @OrderColumn(name = "\"order\"")
    private ParagraphOld paragraph;

    @Column(name = "\"title\"")
    private String title;

    @Convert(converter = ListConverter.class)
    @Column(name = "\"images\"")
    private List<String> images = new ArrayList<>();

    @Convert(converter = ListConverter.class)
    @Column(name = "\"tags\"")
    private List<String> tags = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public ParagraphOld getParagraph() {
        return paragraph;
    }

    public void setParagraph(ParagraphOld paragraph) {
        this.paragraph = paragraph;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
