package org.igye.outline2.controllers;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BeControllerComponentTest extends ControllerComponentTestBase {
    @Test
    public void getNode_databaseIsEmpty_emptyResponseIsReturned() throws Exception {
        //given
        Assert.assertTrue(nodeRepository.findAll().isEmpty());

        //when
        MvcResult res = mvc.perform(
                get("/be/node")
        )
                .andExpect(status().isOk())
                .andReturn();

        //then
        System.out.println(res.getResponse().getContentAsString());
    }

}