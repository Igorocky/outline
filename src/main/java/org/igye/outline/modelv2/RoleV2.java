package org.igye.outline.modelv2;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Data
@NoArgsConstructor
@Entity
public class RoleV2 {
    @Id
    @GeneratedValue
    @Type(type = UUID_CHAR)
    private UUID id;

    @NotNull
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleV2 role = (RoleV2) o;

        return name.equals(role.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
