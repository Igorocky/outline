package org.igye.outline2.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class SerializerOfOptString extends JsonSerializer<OptVal<String>> {
    @Override
    public void serialize(OptVal<String> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.isPresent()) {
            gen.writeString(value.getVal());
        }
    }
}
