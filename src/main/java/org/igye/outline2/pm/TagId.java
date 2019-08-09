package org.igye.outline2.pm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum TagId {
    NAME("name"),
    ICON("icon"),
    IMG_ID("imgId"),
    TEXT("text");

    private static Map<String, TagId> valuesMap = new HashMap<>();

    static {
        for (TagId value : values()) {
            valuesMap.put(value.getTagId(), value);
        }
    }

    private final String tagId;

    TagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagId() {
        return tagId;
    }

    @JsonCreator
    public static TagId fromString(String value) {
        return valuesMap.get(value);
    }

    @JsonValue
    public String toValue() {
        return getTagId();
    }
}
