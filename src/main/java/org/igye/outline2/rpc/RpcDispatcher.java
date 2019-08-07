package org.igye.outline2.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.OutlineUtils;
import org.igye.outline2.exceptions.OutlineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class RpcDispatcher {
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private List<RpcMethodsCollection> rpcMethodsCollections;

    private Map<String, Pair<Object, Method>> methodMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (RpcMethodsCollection rpcMethodsCollection : rpcMethodsCollections) {
            for (Method method : rpcMethodsCollection.getClass().getMethods()) {
                if (method.getAnnotation(RpcMethod.class) != null) {
                    String methodName = method.getName();
                    if (methodMap.containsKey(methodName)) {
                        throw new OutlineException("methodMap.containsKey(\"" + methodName + "\")");
                    }
                    methodMap.put(methodName, Pair.of(rpcMethodsCollection, method));
                }
            }
        }
    }

    public Object dispatchRpcCall(String methodName, JsonNode passedParams) throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        Pair<Object, Method> objectMethodPair = methodMap.get(methodName);
        Method method = objectMethodPair.getRight();
        Parameter[] methodParameters = method.getParameters();
        validatePassedParams(methodParameters, passedParams);
        return method.invoke(
                objectMethodPair.getLeft(),
                prepareArguments(methodName, methodParameters, passedParams)
        );
    }

    private void validatePassedParams(Parameter[] declaredParams, JsonNode passedParams) throws JsonProcessingException {
        Set<String> allParamNames = OutlineUtils.mapToSet(declaredParams, Parameter::getName);
        passedParams.fieldNames().forEachRemaining(passedParamName -> {
            if (!allParamNames.contains(passedParamName)) {
                throw new OutlineException(
                        "!allParamNames.contains(passedParamName): " +
                        "passedParamName is '" + passedParamName + "'," +
                                " allParamNames are [" + StringUtils.join(allParamNames, ",") + "]"
                );
            }
        });
    }

    private Object[] prepareArguments(String methodName,
                                      Parameter[] declaredParams,
                                      JsonNode passedParams) throws JsonProcessingException {
        Object[] arguments = new Object[declaredParams.length];
        for (int i = 0; i < declaredParams.length; i++) {
            Parameter declaredParam = declaredParams[i];
            Class<?> paramType = declaredParam.getType();
            String paramName = declaredParam.getName();
            JsonNode passedParam = passedParams.get(paramName);
            if (paramType.equals(Optional.class)) {
                if (!passedParams.has(paramName)) {
                    arguments[i] = Optional.empty();
                } else if (passedParam.isNull()) {
                    arguments[i] = null;
                } else {
                    ParameterizedType parameterizedType = (ParameterizedType) declaredParam.getParameterizedType();
                    arguments[i] = Optional.of(objectMapper.treeToValue(
                            passedParam,
                            (Class) parameterizedType.getActualTypeArguments()[0]
                    ));
                }
            } else if (passedParam != null) {
                arguments[i] = objectMapper.treeToValue(passedParam, paramType);
            } else {
                throw new OutlineException("Rpc call error: required parameter '" + paramName
                        + "' is not specified for method " + methodName + ".");
            }
        }
        return arguments;
    }
}
