package org.igye.outline.datamigration;

import org.igye.outline.model.Tag;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class TagCollection {
    private Set<Tag> tags = new HashSet<>();

    public Tag getTag(String tagStr) {
        return tags.stream().filter(tag -> tag.getName().equals(tagStr)).findFirst().orElseGet(() -> {
            Tag tag = new Tag(tagStr);
            tags.add(tag);
            return tag;
        });
    }

}
