package org.igye.outline.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REFRESH;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;
import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Topic {
    @Id
    @GeneratedValue
    @Type(type = UUID_CHAR)
    private UUID id;

    @ManyToOne
    private User owner;

    @NotNull
    private String name;

    @NotNull
    @ManyToOne
    private Paragraph paragraph;

    @ManyToMany
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE})
    @JoinTable(name = "TOPIC_TAG")
    private Set<Tag> tags = new HashSet<>();
}
