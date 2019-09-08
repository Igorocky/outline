package org.igye.outline2.chess.manager.state;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

@Getter
public class AsyncWebSocketRpcCall {
    private String methodName;
    private JsonNode params;
}
