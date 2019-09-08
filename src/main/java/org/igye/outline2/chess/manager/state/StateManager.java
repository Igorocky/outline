package org.igye.outline2.chess.manager.state;

import com.fasterxml.jackson.databind.JsonNode;
import org.igye.outline2.rpc.RpcDispatcher;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RpcMethodsCollection
@Component
public class StateManager {
    private Map<UUID, State> states = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RpcDispatcher rpcDispatcher;

    @RpcMethod
    public UUID registerNewBackendState(String stateType) throws IllegalAccessException, IOException, InvocationTargetException {
        UUID newId = UUID.randomUUID();
        State stateObj = (State) applicationContext.getBean(stateType);
        stateObj.setMethodMap(rpcDispatcher.createMethodMap(stateObj));
        states.put(newId, stateObj);
        return newId;
    }

    @RpcMethod
    public void removeBackendState(UUID stateId) {
        states.remove(stateId);
    }

    @RpcMethod
    public Object invokeMethodOnBackendState(UUID stateId, String methodName, JsonNode params) throws IllegalAccessException, IOException, InvocationTargetException {
        return rpcDispatcher.dispatchRpcCall(methodName, params, states.get(stateId).getMethodMap());
    }
}
