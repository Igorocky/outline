package org.igye.outline2.controllers;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.dto.NodeDto;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.controllers.OutlineTestUtils.writeValueAsString;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControllerComponentTestBase extends ComponentTestBase {
    @Autowired
    protected WebApplicationContext wac;
    protected static MockMvc mvc;

    protected Invocable jsAdapter;
    protected static String actualPatchUrl;
    protected static String actualPatchBody;
    private static final String ON_SUCCESS_CALLBACK = "function(response){Java.type('org.igye.outline2.controllers.ControllerComponentTestBase').onSuccess(JSON.stringify(response))}";
    protected static String onSuccessResponse;

    @Before
    public void controllerComponentTestBaseBefore() throws FileNotFoundException, ScriptException {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();

        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine(new String[] { "--language=es6" });
        engine.eval(new FileReader("./src/test/resources/js-test-utils.js"));
        engine.eval(new FileReader("./src/main/webapp/js/be-integration.js"));
        jsAdapter = (Invocable) engine;
    }

    public static String doPatch(String url, String requestBody) throws Exception {
        actualPatchUrl = url;
        actualPatchBody = requestBody;
        return mvc.perform(
                patch(url)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(requestBody)
        )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    public static String doGet(String url) throws Exception {
        return mvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    public static void onSuccess(String response) {
        onSuccessResponse = response;
    }

    protected NodeDto parseNodeDto(MvcResult res) throws IOException {
        return objectMapper.readValue(res.getResponse().getContentAsString(), NodeDto.class);
    }

    protected NodeDto parseNodeDto(String res) throws IOException {
        return objectMapper.readValue(res, NodeDto.class);
    }

    protected Map<String, Object> parseAsMap(MvcResult res) throws IOException {
        return objectMapper.readValue(res.getResponse().getContentAsString(), Map.class);
    }

    protected Map<String, Object> parseAsMap(String res) throws IOException {
        return objectMapper.readValue(res, Map.class);
    }

    protected String invokeJsRpcFunction(String functionName, Object... args) throws ScriptException, NoSuchMethodException {
        onSuccessResponse = null;
        if (args == null) {
            args = new Object[]{null};
        }

        jsAdapter.invokeFunction(
                "doTestCall",
                functionName,
                "["
                        + StringUtils.join(map(args, this::serializeArgument), ",")
                        + ", " + ON_SUCCESS_CALLBACK + "]"
        );
        return onSuccessResponse;
    }

    private String serializeArgument(Object arg) {
        if (arg == null) {
            return "null";
        } else {
            return arg.getClass() == OutlineTestUtils.DoNotSerialize.class
                    ?((OutlineTestUtils.DoNotSerialize)arg).getValue().toString()
                    :writeValueAsString(objectMapper, arg);
        }
    }
}