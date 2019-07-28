package org.igye.outline2.controllers;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.setOf;
import static org.igye.outline2.controllers.OutlineTestUtils.assertNodeInDatabase;
import static org.igye.outline2.controllers.Randoms.randomNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BeControllerComponentTest extends ControllerComponentTestBase {
    private Invocable jsAdapter;

    @Before
    public void beControllerComponentTestBefore() throws FileNotFoundException, ScriptException {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine(new String[] { "--language=es6" });
        engine.eval(new FileReader("./src/test/resources/js-test-utils.js"));
        engine.eval(new FileReader("./src/main/webapp/js/be-integration.js"));
        jsAdapter = (Invocable) engine;
    }

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
        assertEquals(setOf("id", "parentId", "objectClass", "name", "icon", "childNodes"), parseAsMap(res).keySet());
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
        assertEquals(setOf("id", "parentId", "objectClass", "name", "icon"), parseAsMap(res).keySet());
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
        assertEquals(setOf("id", "parentId", "objectClass", "name", "icon"), parseAsMap(res).keySet());
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
    public void getNode_returnsNullForIdAndParentIdOfARootNode() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<UUID> nodeId = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode).storeId(nodeId)
        ;
        rootNodes.get().forEach(nodeRepository::save);

        //when
        MvcResult res = mvc.perform(
                get("/be/node/" + nodeId.get())
        ).andExpect(status().isOk()).andReturn();

        //then
        Map<String, Object> nodeDto = parseAsMap(res);
        assertTrue(nodeDto.containsKey("parentId"));
        assertNull(nodeDto.get("parentId"));
    }

    @Test
    public void getNode_returnsAllRequiredAttributesForTheFakeTopNode() throws Exception {
        //given

        //when
        MvcResult res = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn();

        //then
        Map<String, Object> nodeDto = parseAsMap(res);
        assertNull(nodeDto.get("id"));
        assertEquals(setOf("parentId", "id", "objectClass", "name", "icon"), parseAsMap(res).keySet());
    }

    @Test
    public void patchNode_createsNewRootNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 9, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeIsNullOrderByOrd().stream()
                .map(Node::getId).collect(Collectors.toSet());
        String rootNode = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Node expectedNode = new Node();
        expectedNode.setOrd(existingNodes.size());
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildNode", "[" + rootNode + "]");

        //then
        UUID newNodeId = nodeRepository.findByParentNodeIsNullOrderByOrd().stream()
                .map(Node::getId)
                .filter(id -> !existingNodes.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        assertNodeInDatabase(jdbcTemplate, expectedNode);
    }

    public static void doPatch(String url, String requestBody) throws Exception {
        mvc.perform(
                patch("/be/node")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(requestBody)
        )
                .andExpect(status().isOk());
    }

    private Object invokeJsFunction(String functionName, String arrOfArguments) throws ScriptException, NoSuchMethodException {
        return jsAdapter.invokeFunction("doTestCall", functionName, arrOfArguments);
    }
}