package org.igye.outline2.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.igye.outline2.pm.TagId;

import java.io.IOException;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;

public class SerializerOfOptTagId extends JsonSerializer<OptVal<TagId>> {
    @Override
    public void serialize(OptVal<TagId> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.isPresent()) {
            gen.writeString((String) nullSafeGetter(value.getVal(), tagId->tagId.getTagId()));
        }
    }
}
