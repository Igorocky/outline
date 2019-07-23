package org.igye.outline2.controllers;

import org.igye.outline2.dto.NodeDto;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Map;

import static org.igye.outline2.OutlineUtils.setOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BeControllerComponentTest extends ControllerComponentTestBase {
    @Test
    public void getNode_databaseIsEmptyAndDepthEq1_RootNodeWithEmptyChildrenListIsReturned() throws Exception {
        //given
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        MvcResult res = mvc.perform(
                get("/be/node?depth=1")
        )
                .andExpect(status().isOk())
                .andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals("ROOT_NODE", nodeDto.getObjectClass().get());
        assertTrue(nodeDto.getChildNodes().get().isEmpty());
        assertEquals(setOf("objectClass", "childNodes"), parseAsMap(res).keySet());
    }

    @Test
    public void getNode_databaseIsEmptyAndDepthEq0_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        MvcResult res = mvc.perform(
                get("/be/node?depth=0")
        )
                .andExpect(status().isOk())
                .andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals("ROOT_NODE", nodeDto.getObjectClass().get());
        assertFalse(nodeDto.getChildNodes().isPresent());
        assertEquals(setOf("objectClass"), parseAsMap(res).keySet());
    }

    @Test
    public void getNode_databaseIsEmptyAndDepthIsNotSpecified_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        MvcResult res = mvc.perform(
                get("/be/node")
        )
                .andExpect(status().isOk())
                .andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals("ROOT_NODE", nodeDto.getObjectClass().get());
        assertFalse(nodeDto.getChildNodes().isPresent());
        assertEquals(setOf("objectClass"), parseAsMap(res).keySet());
    }

    @Test
    public void builder() throws Exception {
        Map<String, Object> savedObjects = new NodeTreeBuilder()
                .saveTree("tree")
                .node(
                        n -> n.setName("n1"),
                        n -> n.setCreatedWhen(Instant.now())
                )
                .children(b1 -> b1
                        .node(
                                n -> n.setName("n3")
                        )
                        .node(
                                n -> n.setName("n4")
                        )
                )
                .node(
                        n -> n.setName("n2"),
                        n -> n.setCreatedWhen(Instant.now())
                )
                .getSavedObjects();

        System.out.println(savedObjects);
    }

}