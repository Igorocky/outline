package org.igye.outline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.annotations.CascadeType.*;

@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Topic {
    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
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
    private Set<Tag> tags = new HashSet<>();
}
