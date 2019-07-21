package org.igye.outline2.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.time.Instant;
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
@AllArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Node {
    @Id
    private UUID id = UUID.randomUUID();

    private String name;

    @ManyToOne
    private Node parentNode;

    private int ord;

    @OneToMany(mappedBy = "parentNode")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE})
    private List<Node> childNodes = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
    private Image icon;

    private Instant createdWhen = Instant.now();
    private Instant updatedWhen = Instant.now();
}
