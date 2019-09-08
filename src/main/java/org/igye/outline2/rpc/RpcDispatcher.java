package org.igye.outline2.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.OutlineUtils;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.exceptions.OutlineException;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.igye.outline2.dto.OptVal.ABSENT_OPT_VAL;

@Service
public class RpcDispatcher {
    private JavaType listOfUuidsJavaType;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    @RpcMethodsCollection
    private List<Object> rpcMethodsCollections;

    private Map<String, Pair<Object, Method>> methodMap = new HashMap<>();

    @PostConstruct
    public void init() {
        listOfUuidsJavaType = objectMapper.getTypeFactory().constructType(new TypeReference<List<UUID>>() {});
        for (Object rpcMethodsCollection : rpcMethodsCollections) {
            methodMap.putAll(createMethodMap(rpcMethodsCollection));
        }
    }

    public Object dispatchRpcCall(String methodName, JsonNode passedParams) throws IOException, InvocationTargetException, IllegalAccessException {
        return dispatchRpcCall(methodName, passedParams, methodMap);
    }

    public Object dispatchRpcCall(String methodName, JsonNode passedParams, Map<String, Pair<Object, Method>> methodMap) throws IOException, InvocationTargetException, IllegalAccessException {
        Pair<Object, Method> objectMethodPair = methodMap.get(methodName);
        if (objectMethodPair == null) {
            throw new OutlineException("Could not find RPC method with name " + methodName);
        }
        Method method = objectMethodPair.getRight();
        Parameter[] methodParameters = method.getParameters();
        validatePassedParams(methodName, methodParameters, passedParams);
        return method.invoke(
                objectMethodPair.getLeft(),
                prepareArguments(methodName, methodParameters, passedParams)
        );
    }

    public Map<String, Pair<Object, Method>> createMethodMap(Object rpcMethodsCollection) {
        Map<String, Pair<Object, Method>> methodMap = new HashMap<>();
        for (Method method : AopUtils.getTargetClass(rpcMethodsCollection).getMethods()) {
            if (method.getAnnotation(RpcMethod.class) != null) {
                String methodName = method.getName();
                if (methodMap.containsKey(methodName)) {
                    throw new OutlineException("methodMap.containsKey(\"" + methodName + "\")");
                }
                methodMap.put(methodName, Pair.of(rpcMethodsCollection, method));
            }
        }
        return methodMap;
    }

    private void validatePassedParams(String methodName, Parameter[] declaredParams, JsonNode passedParams) {
        Set<String> allParamNames = OutlineUtils.mapToSet(declaredParams, Parameter::getName);
        passedParams.fieldNames().forEachRemaining(passedParamName -> {
            if (!allParamNames.contains(passedParamName)) {
                throw new OutlineException(
                        "Unknown parameter name '" + passedParamName + "' in " + methodName + ", expected are [" + StringUtils.join(allParamNames, ",") + "]"
                );
            }
        });
    }

    private Object[] prepareArguments(String methodName,
                                      Parameter[] declaredParams,
                                      JsonNode passedParams) throws IOException {
        Object[] arguments = new Object[declaredParams.length];
        for (int i = 0; i < declaredParams.length; i++) {
            Parameter declaredParam = declaredParams[i];
            Class<?> paramType = declaredParam.getType();
            String paramName = declaredParam.getName();
            JsonNode passedParam = passedParams.get(paramName);
            if (paramType.equals(OptVal.class)) {
                if (!passedParams.has(paramName)) {
                    arguments[i] = ABSENT_OPT_VAL;
                } else if (passedParam.isNull()) {
                    arguments[i] = new OptVal<>(null);
                } else {
                    arguments[i] = parseParam(methodName, declaredParam, passedParam);
                }
            } else if (passedParam != null) {
                arguments[i] = parseParam(methodName, declaredParam, passedParam);
            } else {
                Default defaultAnnotation = declaredParam.getAnnotation(Default.class);
                if (defaultAnnotation!=null) {
                    arguments[i] = objectMapper.readValue(defaultAnnotation.value(), (Class) declaredParam.getType());
                } else {
                    throw new OutlineException("Rpc call error: required parameter '" + paramName
                            + "' is not specified for method " + methodName + ".");
                }
            }
        }
        return arguments;
    }

    private Object parseParam(String methodName, Parameter declaredParam, JsonNode passedParam) throws IOException {
        if (passedParam.isNull()) {
            return null;
        }
        Class<?> declaredParamType = declaredParam.getType();
        if (declaredParamType.equals(List.class)) {
            Class typeArgument = getTypeArgument(declaredParam);
            if (typeArgument.equals(UUID.class)) {
                return readValue(passedParam, listOfUuidsJavaType);
            } else {
                throw new OutlineException("Cannot deserialize value for " + declaredParam.getName()
                        + " rpc method parameter in " + methodName + " method.");
            }
        } else if (declaredParamType.equals(OptVal.class)) {
            return objectMapper.treeToValue(passedParam, getTypeArgument(declaredParam));
        } else {
            return objectMapper.treeToValue(passedParam, declaredParamType);
        }
    }

    private Class getTypeArgument(Parameter declaredParam) {
        ParameterizedType parameterizedType = (ParameterizedType) declaredParam.getParameterizedType();
        return (Class) parameterizedType.getActualTypeArguments()[0];
    }

    private Object readValue(JsonNode passedParam, JavaType javaType) throws IOException {
        return objectMapper.readValue(objectMapper.treeAsTokens(passedParam), javaType);
    }
}
