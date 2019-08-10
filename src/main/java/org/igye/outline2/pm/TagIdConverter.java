package org.igye.outline2.pm;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class TagIdConverter implements AttributeConverter<TagId, String> {
    @Override
    public String convertToDatabaseColumn(TagId tagId) {
        if (tagId == null) {
            return null;
        }
        return tagId.getTagId();
    }

    @Override
    public TagId convertToEntityAttribute(String dbData) {
        return TagId.fromString(dbData);
    }
}
