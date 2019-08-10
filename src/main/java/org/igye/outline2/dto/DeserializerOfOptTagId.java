package org.igye.outline2.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.igye.outline2.pm.TagId;

import java.io.IOException;

public class DeserializerOfOptTagId extends JsonDeserializer<OptVal<TagId>> {
    @Override
    public OptVal<TagId> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String tagIdStr = jsonParser.getText();
        if (tagIdStr == null) {
            return new OptVal<>(null);
        } else {
            return new OptVal<>(TagId.fromString(tagIdStr));
        }
    }
}
