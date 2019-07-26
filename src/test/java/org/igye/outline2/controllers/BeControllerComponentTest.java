package org.igye.outline2.controllers;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.io.FileReader;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.OutlineUtils.map;
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
        nodeRepository.findByParentNodeIsNullOrderByOrd().forEach(nodeRepository::delete);
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
        nodeRepository.findByParentNodeIsNullOrderByOrd().forEach(nodeRepository::delete);
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
        nodeRepository.findByParentNodeIsNullOrderByOrd().forEach(nodeRepository::delete);
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
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<UUID> nodeId = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<UUID> node1Id = new ObjectHolder<>();
        ObjectHolder<UUID> node2Id = new ObjectHolder<>();
        ObjectHolder<UUID> node3Id = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeId(nodeId)
                .children(b1->b1
                        .node(randomNode).storeId(node1Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).storeId(node2Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).storeId(node3Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                )
                .node(randomNode)
        ;
        rootNodes.get().forEach(nodeRepository::save);

        //when
        MvcResult res = mvc.perform(
                get("/be/node/" + nodeId.get() + "?depth=1")
        ).andExpect(status().isOk()).andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        List<NodeDto> childNodes = nodeDto.getChildNodes().get();
        assertEquals(
                listOf(node1Id.get(), node2Id.get(), node3Id.get()),
                map(childNodes, NodeDto::getId)
        );
        childNodes.forEach(c -> assertFalse(c.getChildNodes().isPresent()));
    }

    @Test
    public void getNode_nodeHas2LevelsOfChildrenAndDepth0Requested_noChildNodesAttributeReturned() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<UUID> nodeId = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<UUID> node1Id = new ObjectHolder<>();
        ObjectHolder<UUID> node2Id = new ObjectHolder<>();
        ObjectHolder<UUID> node3Id = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeId(nodeId)
                .children(b1->b1
                        .node(randomNode).storeId(node1Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).storeId(node2Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).storeId(node3Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                )
                .node(randomNode)
        ;
        rootNodes.get().forEach(nodeRepository::save);

        //when
        MvcResult res = mvc.perform(
                get("/be/node/" + nodeId.get() + "?depth=0")
        ).andExpect(status().isOk()).andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertFalse(nodeDto.getChildNodes().isPresent());
    }

    @Test
    public void getNode_nodeHas2LevelsOfChildrenAndDepthIsNotRequested_noChildNodesAttributeReturned() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<UUID> nodeId = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<UUID> node1Id = new ObjectHolder<>();
        ObjectHolder<UUID> node2Id = new ObjectHolder<>();
        ObjectHolder<UUID> node3Id = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeId(nodeId)
                .children(b1->b1
                        .node(randomNode).storeId(node1Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).storeId(node2Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).storeId(node3Id)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                )
                .node(randomNode)
        ;
        rootNodes.get().forEach(nodeRepository::save);

        //when
        MvcResult res = mvc.perform(
                get("/be/node/" + nodeId.get())
        ).andExpect(status().isOk()).andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertFalse(nodeDto.getChildNodes().isPresent());
    }

    @Test
    public void patchNode_newChildNodeAppendedToRootNode_newChildNodeAppearsInDatabaseAndReturnedInResponse() throws Exception {
        //given
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine(new String[] { "--language=es6" });

        engine.eval(new FileReader("./src/main/webapp/js/libs/underscore-min-1.9.1.js"));
        engine.eval(new FileReader("./src/test/resources/js-test-utils.js"));
        engine.eval(new FileReader("./src/main/webapp/js/state-change.js"));
        Invocable inv = (Invocable) engine;
        Object result = inv.invokeFunction("doTestCall","addChildNode", "[{\"objectClass\":\"ROOT_NODE\",\"childNodes\":[]}]");
        System.out.println("result = " + result);
    }

}