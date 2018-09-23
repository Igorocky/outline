package org.igye.outline.modelv2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class UserV2 {
    @Id
    @GeneratedValue
    @Type(type = UUID_CHAR)
    private UUID id;

    @NotNull
    @Column(unique = true, nullable = false)
    private String name;
    @NotNull
    private String password;
    @NotNull
    private Boolean locked = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLE_V2")
    private Set<RoleV2> roles = new HashSet<>();
}
