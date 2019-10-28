package org.igye.outline2.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

@Getter
public class AsyncWebSocketRpcCall {
    private String methodName;
    private JsonNode params;
}
