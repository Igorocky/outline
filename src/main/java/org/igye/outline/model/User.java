package org.igye.outline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    @Column(unique = true)
    private String name;
    @NotNull
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();
}
