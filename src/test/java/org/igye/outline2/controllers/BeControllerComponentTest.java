package org.igye.outline2.controllers;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.OutlineUtils.setOf;
import static org.igye.outline2.controllers.Randoms.randomNode;
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
    public void getNode_nodeHas2LevelsOfChildrenAndDepth1Requested_onlyTheFirstLevelReturned() throws Exception {
        //given
        List<Image> images = Randoms.list(100, Randoms::image);
        images.forEach(imageRepository::save);
        ObjectHolder<UUID> nodeId = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node1 = new ObjectHolder<>();
        ObjectHolder<Node> node2 = new ObjectHolder<>();
        ObjectHolder<Node> node3 = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode(images))
                .node(randomNode(images)).storeId(nodeId)
                .children(b1->b1
                        .node(randomNode(images)).storeNode(node1)
                        .children(b2->b2
                                .node(randomNode(images))
                                .node(randomNode(images))
                                .node(randomNode(images))
                        )
                        .node(randomNode(images)).storeNode(node2)
                        .children(b2->b2
                                .node(randomNode(images))
                                .node(randomNode(images))
                                .node(randomNode(images))
                        )
                        .node(randomNode(images)).storeNode(node3)
                        .children(b2->b2
                                .node(randomNode(images))
                                .node(randomNode(images))
                                .node(randomNode(images))
                        )
                )
                .node(randomNode(images))
        ;
        rootNodes.get().forEach(nodeRepository::save);

        //when
        MvcResult res = mvc.perform(
                get("/be/node/" + nodeId)
        ).andExpect(status().isOk()).andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals(
                listOf(node1.get().getId(), node2.get().getId(), node3.get().getId()),
                nodeDto.getChildNodes().get()
        );
        nodeDto.getChildNodes().get().forEach(c -> assertFalse(c.getChildNodes().isPresent()));
    }

}