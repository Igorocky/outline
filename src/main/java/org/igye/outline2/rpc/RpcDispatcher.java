package org.igye.outline2.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.OutlineUtils;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.exceptions.OutlineException;
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
import java.util.Optional;
import java.util.Set;

import static org.igye.outline2.dto.OptVal.ABSENT_OPT_VAL;

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

    public Object dispatchRpcCall(String methodName, JsonNode passedParams) throws IOException, InvocationTargetException, IllegalAccessException {
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
                    ParameterizedType parameterizedType = (ParameterizedType) declaredParam.getParameterizedType();
                    arguments[i] = new OptVal<>(objectMapper.treeToValue(
                            passedParam,
                            (Class) parameterizedType.getActualTypeArguments()[0]
                    ));
                }
            } else if (passedParam != null) {
                arguments[i] = objectMapper.treeToValue(passedParam, paramType);
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
}
