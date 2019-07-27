package org.igye.outline2.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hibernate.annotations.CascadeType.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
public class Node {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    private Node parentNode;

    private String name;

    private int ord;

    @OneToMany(mappedBy = "parentNode")
    @Cascade({PERSIST, REFRESH, SAVE_UPDATE, MERGE, REMOVE, DELETE})
    private List<Node> childNodes = new ArrayList<>();

    @ManyToOne
    private Image icon;

    private Instant createdWhen = Instant.now();

    public void addChild(Node child) {
        child.setParentNode(this);
        childNodes.add(child);
        child.setOrd(childNodes.size()-1);
    }
}
