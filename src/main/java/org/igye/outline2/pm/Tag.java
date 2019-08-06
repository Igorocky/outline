package org.igye.outline2.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.OutlineUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Tag {
    private TagId tagId;
    @ManyToOne
    @JoinColumn(name = "ref")
    private Node ref;
    private String value;

    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + tagId +
                ", ref=" + OutlineUtils.nullSafeGetter(ref, n->n.getId()) +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(tagId, tag.tagId) &&
                Objects.equals(
                        OutlineUtils.nullSafeGetter(ref, n->n.getId()),
                        OutlineUtils.nullSafeGetter(tag.ref, n->n.getId())) &&
                Objects.equals(value, tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, ref, value);
    }
}
