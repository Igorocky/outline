package org.igye.outline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"name"})
@Entity
public class Role {
    @Id
    @Type(type = UUID_CHAR)
    private UUID id = UUID.randomUUID();

    @NotNull
    private String name;
}
