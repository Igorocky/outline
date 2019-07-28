package org.igye.outline2.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.igye.outline2.dto.NodeDto;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Map;

public class ControllerComponentTestBase extends ComponentTestBase {
    @Autowired
    protected WebApplicationContext wac;

    protected static MockMvc mvc;

    @Before
    public void controllerComponentTestBaseBefore() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    protected NodeDto parseNodeDto(MvcResult res) throws IOException {
        return objectMapper.readValue(res.getResponse().getContentAsString(), NodeDto.class);
    }

    protected Map<String, Object> parseAsMap(MvcResult res) throws IOException {
        return objectMapper.readValue(res.getResponse().getContentAsString(), Map.class);
    }
}