package org.igye.outline2.chess.manager.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.exceptions.OutlineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

@Log
public abstract class State {
    private Map<String, Pair<Object, Method>> methodMap;
    private WebSocketSession session;

    @Autowired
    private ObjectMapper mapper;

    public void setMethodMap(Map<String, Pair<Object, Method>> methodMap) {
        this.methodMap = methodMap;
    }

    public Map<String, Pair<Object, Method>> getMethodMap() {
        return methodMap;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public void closeSession() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException ex) {
                log.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        session = null;
    }

    protected void sendMessageToFe(Object msg) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
            } catch (IOException ex) {
                throw new OutlineException(ex);
            }
        }
    }
}
