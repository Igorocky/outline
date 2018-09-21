package org.igye.outline.modelv2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
public class ContentV2 {
    @Id
    @GeneratedValue
    @Type(type = UUID_CHAR)
    private UUID id;

    @ManyToOne
    private UserV2 owner;

    @ManyToOne
    private TopicV2 topic;
}
