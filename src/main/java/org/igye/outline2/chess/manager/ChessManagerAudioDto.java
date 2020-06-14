package org.igye.outline2.chess.manager;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChessManagerAudioStateDto.class, name = "state"),
        @JsonSubTypes.Type(value = ChessManagerAudioMsgDto.class, name = "msg")
})
public interface ChessManagerAudioDto {
}
