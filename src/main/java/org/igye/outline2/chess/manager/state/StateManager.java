package org.igye.outline2.chess.manager.state;

import com.fasterxml.jackson.databind.JsonNode;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.rpc.RpcDispatcher;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RpcMethodsCollection
@Component
public class StateManager {
    private Map<UUID, State> states = new HashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RpcDispatcher rpcDispatcher;

    @RpcMethod
    public UUID createNewBackendState(String stateType) throws IllegalAccessException, IOException, InvocationTargetException {
        UUID newId = UUID.randomUUID();
        State stateObj = (State) applicationContext.getBean(stateType);
        stateObj.setMethodMap(rpcDispatcher.createMethodMap(stateObj));
        states.put(newId, stateObj);
        return newId;
    }

    @RpcMethod
    public void removeBackendState(UUID stateId) {
        State stateObj = getStateObject(stateId);
        stateObj.closeSession();
        states.remove(stateId);
    }

    public void invokeMethodOnBackendState(UUID stateId, String methodName, JsonNode params) {
        executorService.submit(() -> {
            final State stateObject = getStateObject(stateId);
            try {
                Object result = rpcDispatcher.dispatchRpcCall(methodName, params, stateObject.getMethodMap());
                if (result != null) {
                    stateObject.sendMessageToFe(result);
                }
            } catch (Exception ex) {
                throw new OutlineException(ex);
            }
        });
    }

    public void bindSessionToState(UUID stateId, WebSocketSession session) {
        final State stateObject = getStateObject(stateId);
        stateObject.closeSession();
        stateObject.setSession(session);
    }

    public void unbindSessionFromState(UUID stateId) {
        getStateObject(stateId).setSession(null);
    }

    private State getStateObject(UUID stateId) {
        return states.get(stateId);
    }
}
