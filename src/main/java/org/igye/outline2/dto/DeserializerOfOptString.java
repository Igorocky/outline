package org.igye.outline2.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class DeserializerOfOptString extends JsonDeserializer<OptVal<String>> {
    @Override
    public OptVal<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return new OptVal<>(jsonParser.getText());
    }
}
