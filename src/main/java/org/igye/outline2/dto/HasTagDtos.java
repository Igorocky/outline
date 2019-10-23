package org.igye.outline2.dto;

import org.igye.outline2.OutlineUtils;

import java.util.ArrayList;
import java.util.List;

public interface HasTagDtos {
    List<TagDto> getTags();

    default String getTagSingleValue(String tagId) {
        return OutlineUtils.getSingleValue(getTagsValues(tagId));
    }

    default List<String> getTagsValues(String tagId) {
        List<String> values = new ArrayList<>();
        getTags().forEach(tag -> {
            if (tag.getTagId().getVal().equals(tagId)) {
                values.add(tag.getValue().getVal());
            }
        });
        return values;
    }

    default List<TagDto> getTags(String tagId) {
        List<TagDto> tags = new ArrayList<>();
        getTags().forEach(tag -> {
            if (tag.getTagId().getVal().equals(tagId)) {
                tags.add(tag);
            }
        });
        return tags;
    }
}
