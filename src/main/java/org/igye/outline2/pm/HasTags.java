package org.igye.outline2.pm;

import org.igye.outline2.OutlineUtils;

import java.util.ArrayList;
import java.util.List;

import static org.igye.outline2.OutlineUtils.getSingleValue;

public interface HasTags {
    List<Tag> getTags();
    void addTag(Tag tag);

    default String getTagSingleValue(TagId tagId) {
        List<String> values = new ArrayList<>();
        getTags().forEach(tag -> {
            if (tag.getTagId().equals(tagId)) {
                values.add(tag.getValue());
            }
        });
        return getSingleValue(values);
    }

    default List<Tag> getTags(TagId tagId) {
        List<Tag> tags = new ArrayList<>();
        getTags().forEach(tag -> {
            if (tag.getTagId().equals(tagId)) {
                tags.add(tag);
            }
        });
        return tags;
    }

    default Tag getTagSingle(TagId tagId) {
        return getSingleValue(getTags(tagId));
    }

    default void setTagSingleValue(TagId tagId, String value) {
        Tag existingTag = getTagSingle(tagId);
        if (existingTag == null) {
            if (value != null) {
                addTag(Tag.builder().tagId(tagId).value(value).build());
            }
        } else {
            if (value != null) {
                existingTag.setValue(value);
            } else {
                existingTag.delete();
            }
        }
    }

    default void removeTags(TagId tagId) {
        getTags(tagId).forEach(Tag::delete);
    }
}
