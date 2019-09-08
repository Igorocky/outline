package org.igye.outline2.chess.manager.state;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.util.Map;

public abstract class State {
    private Map<String, Pair<Object, Method>> methodMap;

    public void setMethodMap(Map<String, Pair<Object, Method>> methodMap) {
        this.methodMap = methodMap;
    }

    public Map<String, Pair<Object, Method>> getMethodMap() {
        return methodMap;
    }
}
