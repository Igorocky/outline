package org.igye.outline2.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;

public class SerializerOfOptUuid extends JsonSerializer<OptVal<UUID>> {
    @Override
    public void serialize(OptVal<UUID> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.isPresent()) {
            gen.writeString((String) nullSafeGetter(value.getVal(), uuid->uuid.toString()));
        }
    }
}
