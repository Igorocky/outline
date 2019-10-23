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
    private String tagId;
    @NotNull
    private String value;

    public void delete() {
        node.detachTag(this);
    }
}
