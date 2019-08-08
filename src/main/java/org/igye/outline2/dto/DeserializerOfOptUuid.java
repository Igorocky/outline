package org.igye.outline2.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.UUID;

public class DeserializerOfOptUuid extends JsonDeserializer<OptVal<UUID>> {
    @Override
    public OptVal<UUID> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String uuidStr = jsonParser.getText();
        if (uuidStr == null) {
            return new OptVal<>(null);
        } else {
            return new OptVal<>(UUID.fromString(uuidStr));
        }
    }
}
