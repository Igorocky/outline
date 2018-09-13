package org.igye.outline.modelv2;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class NodeV2 {
    @Id
    @GeneratedValue
    @Type(type = UUID_CHAR)
    private UUID id;

    @ManyToOne
    private UserV2 owner;

    @NotNull
    private String name;

    @NotNull
    @ManyToOne
    private NodeV2 parentNode;
}
