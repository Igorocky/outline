package org.igye.outline.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REFRESH;
import static org.hibernate.annotations.CascadeType.REMOVE;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ENG_TEXT")
public class EngText extends Node {
    @Column(name = "TEXT")
    private String text;

    @OneToMany(mappedBy = "engText", orphanRemoval = true)
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    private List<Word> words = new ArrayList<>();

    @Column(name = "IGNORE_LIST")
    private String ignoreList;
    @Column(name = "LEARN_GROUP")
    private String learnGroup;

    public void addWord(Word word) {
        Hibernate.initialize(getWords());
        getWords().add(word);
        word.setEngText(this);
        word.setOwner(getOwner());
    }

    public void detachWordById(UUID id) {
        Hibernate.initialize(getWords());
        int idx = 0;
        while (idx < getWords().size()) {
            Word word = getWords().get(idx);
            if (word.getId().equals(id)) {
                word.setEngText(null);
                getWords().remove(idx);
            } else {
                idx++;
            }
        }
    }
}
