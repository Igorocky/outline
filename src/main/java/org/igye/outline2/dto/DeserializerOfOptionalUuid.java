package org.igye.outline2.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class DeserializerOfOptionalUuid extends JsonDeserializer<Optional<UUID>> {
    @Override
    public Optional<UUID> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String uuidStr = jsonParser.getText();
        if (uuidStr == null) {
            return null;
        } else {
            return Optional.of(UUID.fromString(uuidStr));
        }
    }
}
