package org.igye.outline.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Content {
    @Id
    @Type(type = UUID_CHAR)
    private UUID id = UUID.randomUUID();

    @ManyToOne
    private User owner;

    @ManyToOne
    private Topic topic;
}
