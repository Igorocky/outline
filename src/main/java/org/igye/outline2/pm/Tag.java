package org.igye.outline2.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Audited
public class Tag {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne
    private Node node;

    @NotNull
    private TagId tagId;
    @NotNull
    private String value;

    public void delete() {
        node.detachTag(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id.equals(tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
