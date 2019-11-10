package org.igye.outline2.pm;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.igye.outline2.common.OutlineUtils.getSingleValueOrNull;

public interface HasTags {
    List<Tag> getTags();
    void addTag(Tag tag);

    default UUID addTag(String tagId, String value) {
        final UUID id = UUID.randomUUID();
        addTag(Tag.builder()
                .id(id)
                .tagId(tagId)
                .value(value)
                .build());
        return id;
    }

    default String getTagSingleValue(String tagId) {
        List<String> values = new ArrayList<>();
        getTags().forEach(tag -> {
            if (tag.getTagId().equals(tagId)) {
                values.add(tag.getValue());
            }
        });
        return getSingleValueOrNull(values);
    }

    default List<Tag> getTags(String tagId) {
        List<Tag> tags = new ArrayList<>();
        getTags().forEach(tag -> {
            if (tag.getTagId().equals(tagId)) {
                tags.add(tag);
            }
        });
        return tags;
    }

    default Tag getTagSingle(String tagId) {
        return getSingleValueOrNull(getTags(tagId));
    }

    default void setTagSingleValue(String tagId, String value) {
        Tag existingTag = getTagSingle(tagId);
        if (existingTag == null) {
            addTag(Tag.builder().tagId(tagId).value(value).build());
        } else {
            existingTag.setValue(value);
        }
    }

    default void removeTags(String tagId) {
        getTags(tagId).forEach(Tag::delete);
    }
}
