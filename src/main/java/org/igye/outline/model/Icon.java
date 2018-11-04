package org.igye.outline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Icon {
    @Id
    @Type(type = UUID_CHAR)
    private UUID id = UUID.randomUUID();

    @ManyToOne
    private User owner;
}
