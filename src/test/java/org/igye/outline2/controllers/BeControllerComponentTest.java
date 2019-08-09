package org.igye.outline2.controllers;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.controllers.OutlineTestUtils.DoNotSerialize;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.Clipboard;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClass;
import org.igye.outline2.pm.TagId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.mapOf;
import static org.igye.outline2.OutlineUtils.setOf;
import static org.igye.outline2.controllers.OutlineTestUtils.assertMapsEqual;
import static org.igye.outline2.controllers.OutlineTestUtils.assertNodeInDatabase;
import static org.igye.outline2.controllers.OutlineTestUtils.doNotSerialize;
import static org.igye.outline2.controllers.OutlineTestUtils.saveNodeTreeToDatabase;
import static org.igye.outline2.controllers.OutlineTestUtils.writeValueAsString;
import static org.igye.outline2.controllers.Randoms.randomNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BeControllerComponentTest extends ControllerComponentTestBase {
    private static final String ON_SUCCESS_CALLBACK = "function(response){Java.type('org.igye.outline2.controllers.BeControllerComponentTest').onSuccess(response)}";
    private static String actualPatchUrl;
    private static String actualPatchBody;
    private static String onSuccessResponse;

    private Invocable jsAdapter;
    @Autowired
    private Clipboard clipboard;

    @Before
    public void beControllerComponentTestBefore() throws FileNotFoundException, ScriptException {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine(new String[] { "--language=es6" });
        engine.eval(new FileReader("./src/test/resources/js-test-utils.js"));
        engine.eval(new FileReader("./src/main/webapp/js/be-integration.js"));
        jsAdapter = (Invocable) engine;
    }

    @Test public void getNode_databaseIsEmptyAndDepthEq1_RootNodeWithEmptyChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        String res = invokeJsRpcFunction("getNode", mapOf("depth", 1));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals(NodeClass.TOP_CONTAINER, nodeDto.getClazz());
        assertTrue(nodeDto.getChildNodes().isEmpty());
        assertEquals(
                setOf("id", "clazz", "tags", "parentId", "childNodes", "path"),
                parseAsMap(res).keySet()
        );
    }
    @Test public void getNode_databaseIsEmptyAndDepthEq0_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        String res = invokeJsRpcFunction("getNode", mapOf("depth", 0));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals(NodeClass.TOP_CONTAINER, nodeDto.getClazz());
        assertNull(nodeDto.getChildNodes());
        assertEquals(setOf("id", "clazz", "tags", "parentId", "path"), parseAsMap(res).keySet());
    }
    @Test public void getNode_databaseIsEmptyAndDepthIsNotSpecified_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        String res = invokeJsRpcFunction("getNode", Collections.emptyMap());

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals(NodeClass.TOP_CONTAINER, nodeDto.getClazz());
        assertNull(nodeDto.getChildNodes());
        assertEquals(setOf("id", "clazz", "tags", "parentId", "path"), parseAsMap(res).keySet());
    }
    @Test public void getNode_nodeHas2LevelsOfChildrenAndDepth1Requested_onlyTheFirstLevelReturned() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
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
        String res = invokeJsRpcFunction("getNode", mapOf("id", nodeId.get(), "depth", 1));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        List<NodeDto> childNodes = nodeDto.getChildNodes();
        assertEquals(
                listOf(node1Id.get(), node2Id.get(), node3Id.get()),
                map(childNodes, NodeDto::getId)
        );
        childNodes.forEach(c -> assertNull(c.getChildNodes()));
    }
    @Test public void getNode_nodeHas2LevelsOfChildrenAndDepth0Requested_noChildNodesAttributeReturned() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
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
        String res = invokeJsRpcFunction("getNode", mapOf("id", nodeId.get(), "depth", 0));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertNull(nodeDto.getChildNodes());
    }
    @Test public void getNode_nodeHas2LevelsOfChildrenAndDepthIsNotRequested_noChildNodesAttributeReturned() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
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
        String res = invokeJsRpcFunction("getNode", mapOf("id", nodeId.get()));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertNull(nodeDto.getChildNodes());
    }
    @Test public void getNode_returnsNullForParentIdOfARootNode() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<UUID> nodeId = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode).storeId(nodeId)
        ;
        rootNodes.get().forEach(nodeRepository::save);

        //when
        String res = invokeJsRpcFunction("getNode", mapOf("id", nodeId.get()));

        //then
        Map<String, Object> nodeDto = parseAsMap(res);
        assertTrue(nodeDto.containsKey("parentId"));
        assertNull(nodeDto.get("parentId"));
    }
    @Test public void getNode_returnsAllRequiredAttributesForTheFakeTopNode() throws Exception {
        //when
        String res = invokeJsRpcFunction("getNode", Collections.emptyMap());

        //then
        Map<String, Object> nodeDto = parseAsMap(res);
        assertNull(nodeDto.get("id"));
        assertEquals(setOf("id", "clazz", "tags", "parentId", "path"), parseAsMap(res).keySet());
    }
    @Test public void getNodeByIdInJs_nullIsPassed_rootNodeWithDepth1IsRequested() throws Exception {
        //given
        actualPatchUrl = null;
        actualPatchBody = null;

        //when
        invokeJsRpcFunction("getNodeById", null);

        //then
        assertEquals("/be/rpc/rpcGetNode", actualPatchUrl);
        assertMapsEqual(
                mapOf("id", null, "depth", 1, "includeCanPaste", true),
                parseAsMap(actualPatchBody)
        );
    }
    @Test public void getNodeByIdInJs_undefinedIsPassed_rootNodeWithDepth1IsRequested() throws Exception {
        //given
        actualPatchUrl = null;
        actualPatchBody = null;

        //when
        invokeJsRpcFunction("getNodeById", doNotSerialize("undefined"));

        //then
        assertEquals("/be/rpc/rpcGetNode", actualPatchUrl);
        assertMapsEqual(
                mapOf("depth", 1, "includeCanPaste", true),
                parseAsMap(actualPatchBody)
        );
    }
    @Test public void getNodeByIdInJs_UuidIsPassed_nodeWithTheGivenUuidWithDepth1IsRequested() throws Exception {
        //given
        actualPatchUrl = null;
        actualPatchBody = null;
        Node node = new Node();
        nodeRepository.save(node);

        //when
        invokeJsRpcFunction("getNodeById", node.getId());

        //then
        assertEquals("/be/rpc/rpcGetNode", actualPatchUrl);
        assertMapsEqual(
                mapOf("id", node.getId().toString(), "depth", 1, "includeCanPaste", true),
                parseAsMap(actualPatchBody)
        );
    }
    @Test public void patchNode_createsNewRootNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 9, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeId(null).stream()
                .map(Node::getId).collect(Collectors.toSet());
        String topFakeNode = invokeJsRpcFunction("getNode", Collections.emptyMap());
        Node expectedNode = new Node();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsRpcFunction("createChildNode", doNotSerialize(topFakeNode));

        //then
        UUID newNodeId = nodeRepository.findByParentNodeId(null).stream()
                .map(Node::getId)
                .filter(id -> !existingNodes.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        assertNodeInDatabase(jdbcTemplate, expectedNode);
    }
    @Test public void patchNode_createsNewRootTextNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 10, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeId(null).stream()
                .map(Node::getId).collect(Collectors.toSet());
        String topFakeNode = invokeJsRpcFunction("getNode", Collections.emptyMap());
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClass.TEXT);
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsRpcFunction("createChildTextNode", doNotSerialize(topFakeNode));

        //then
        UUID newNodeId = nodeRepository.findByParentNodeId(null).stream()
                .map(Node::getId)
                .filter(id -> !existingNodes.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        assertNodeInDatabase(jdbcTemplate, expectedNode);
    }
    @Test public void patchNode_createsNewRootImageNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 10, 10, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeId(null).stream()
                .map(Node::getId).collect(Collectors.toSet());
        String topNode = invokeJsRpcFunction("getNodeById", null);
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClass.IMAGE);
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsRpcFunction("createChildImageNode", doNotSerialize(topNode));

        //then
        UUID newNodeId = nodeRepository.findByParentNodeId(null).stream()
                .map(Node::getId)
                .filter(id -> !existingNodes.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        assertNodeInDatabase(jdbcTemplate, expectedNode);
    }
    @Test public void patchNode_createsNewInnerNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 9, 8, 11);
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<Node> currNode = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeNode(currNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                )
                .node(randomNode)
        ;
        rootNodes.get().forEach(nodeRepository::save);
        String currNodeStr = invokeJsRpcFunction("getNodeById", currNode.get().getId());
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new Node();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsRpcFunction("createChildNode", doNotSerialize(currNodeStr));

        //then
        UUID newNodeId = nodeRepository.findAll().stream()
                .map(Node::getId)
                .filter(id -> !allNodeIdsBeforeTest.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        currNode.get().addChild(expectedNode);
        assertNodeInDatabase(jdbcTemplate, currNode.get());
    }
    @Test public void patchNode_createsNewInnerTextNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 12, 8, 11);
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<Node> currNode = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeNode(currNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                )
                .node(randomNode)
        ;
        rootNodes.get().forEach(nodeRepository::save);
        String currNodeStr = invokeJsRpcFunction("getNodeById", currNode.get().getId());
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClass.TEXT);
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsRpcFunction("createChildTextNode", doNotSerialize(currNodeStr));

        //then
        UUID newNodeId = nodeRepository.findAll().stream()
                .map(Node::getId)
                .filter(id -> !allNodeIdsBeforeTest.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        currNode.get().addChild(expectedNode);
        assertNodeInDatabase(jdbcTemplate, currNode.get());
    }
    @Test public void patchNode_createsNewInnerImageNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 12, 8, 11);
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<Node> currNode = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeNode(currNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        String currNodeStr = invokeJsRpcFunction("getNodeById", currNode.get().getId());
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClass.IMAGE);
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsRpcFunction("createChildImageNode", doNotSerialize(currNodeStr));

        //then
        UUID newNodeId = nodeRepository.findAll().stream()
                .map(Node::getId)
                .filter(id -> !allNodeIdsBeforeTest.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        currNode.get().addChild(expectedNode);
        assertNodeInDatabase(jdbcTemplate, currNode.get());
    }
    @Test public void patchNode_modifiesNameTagFromNullToSomeStr() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(node -> node.removeTags(TagId.NAME));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        final String expectedName = "adfdf asdf asdf asdf asdf";
        innerNode.setTagSingleValue(TagId.NAME, expectedName);

        //when
        invokeJsRpcFunction("updateNodeName", innerNode.getId(), expectedName);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesNameTagFromSomeStrToNull() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(
                node -> node.setTagSingleValue(TagId.NAME, "okjhhfafadfd")
        );
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        innerNode.removeTags(TagId.NAME);

        //when
        invokeJsRpcFunction("updateNodeName", innerNode.getId(), null);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesIconTagFromNullToSomeUuid() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(node -> node.removeTags(TagId.ICON));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        Node expectedIcon = Randoms.randomNode(n->n.setClazz(NodeClass.IMAGE));
        nodeRepository.save(expectedIcon);
        innerNode.setTagSingleValue(TagId.ICON, expectedIcon);

        //when
        invokeJsRpcFunction("updateNodeIcon", innerNode.getId(), expectedIcon.getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesIconTagFromSomeUuidToNull() throws Exception {
        //given
        Node existingIcon = Randoms.randomNode(n->n.setClazz(NodeClass.IMAGE));
        nodeRepository.save(existingIcon);
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(
                node -> node.setTagSingleValue(TagId.ICON, existingIcon)
        );
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        innerNode.removeTags(TagId.ICON);

        //when
        invokeJsRpcFunction("updateNodeIcon", innerNode.getId(), null);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesTextTagFromNullToSomeStr() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(n -> n.removeTags(TagId.TEXT));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        final String expectedText = "pldfnsxnbc agfsd f";
        innerNode.setTagSingleValue(TagId.TEXT, expectedText);

        //when
        invokeJsRpcFunction("updateTextNodeText", innerNode.getId(), expectedText);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesTextTagFromSomeStrToNull() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(n -> n.setTagSingleValue(TagId.TEXT, "plkjj"));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        innerNode.removeTags(TagId.TEXT);

        //when
        invokeJsRpcFunction("updateTextNodeText", innerNode.getId(), null);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void reorderNode_movesStartNodeUp() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeUp", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeUp() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(2);
        parent.getChildNodes().add(1, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeUp", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeUp() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(4);
        parent.getChildNodes().add(3, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeUp", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesStartNodeDown() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(0);
        parent.getChildNodes().add(1, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeDown", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeDown() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(2);
        parent.getChildNodes().add(3, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeDown", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeDown() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeDown", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesStartNodeToStart() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeToStart", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeToStart() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(2);
        parent.getChildNodes().add(0, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeToStart", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeToStart() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(4);
        parent.getChildNodes().add(0, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeToStart", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesStartNodeToEnd() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(0);
        parent.getChildNodes().add(4, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeToEnd", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeToEnd() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                        .node(randomNode)
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(2);
        parent.getChildNodes().add(4, nodeToMove.get());

        //when
        invokeJsRpcFunction("moveNodeToEnd", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeToEnd() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeToEnd", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeUp() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeUp", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeDown() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeDown", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeToStart() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeToStart", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeToEnd() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("moveNodeToEnd", nodeToMove.get().getId());

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseWhenClipboardIsEmpty() throws Exception {
        //given
        onSuccessResponse = null;
        clipboard.setNodeIds(null);
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMoveTo = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMoveTo)
                        .children(b2 -> b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("canPasteNodesFromClipboard", nodeToMoveTo.get().getId());

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseForPastingIntoOneOfNodesBeingMoved() throws Exception {
        //given
        onSuccessResponse = null;
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove3 = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .children(b2 -> b2
                                .node(randomNode).storeNode(nodeToMove1)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove2)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove3)
                        )
                        .node(randomNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("putNodeIdsToClipboard", Arrays.asList(
                nodeToMove1.get().getId(),
                nodeToMove2.get().getId(),
                nodeToMove3.get().getId()
        ));
        invokeJsRpcFunction("canPasteNodesFromClipboard", nodeToMove2.get().getId());

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseForPastingIntoChildNode() throws Exception {
        //given
        onSuccessResponse = null;
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMoveTo = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(nodeToMove1)
                        .children(b2 -> b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMoveTo)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).storeNode(nodeToMove2)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("putNodeIdsToClipboard",
                Arrays.asList(nodeToMove1.get().getId(), nodeToMove2.get().getId())
        );
        invokeJsRpcFunction("canPasteNodesFromClipboard", nodeToMoveTo.get().getId());

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseAfterPasteWasDone() throws Exception {
        //given
        onSuccessResponse = null;
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMoveTo = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .children(b2 -> b2
                                .node(randomNode).storeNode(nodeToMove1)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove2)
                        )
                        .node(randomNode).storeNode(nodeToMoveTo)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        invokeJsRpcFunction(
                "putNodeIdsToClipboard",
                Arrays.asList(nodeToMove1.get().getId(), nodeToMove2.get().getId())
        );
        invokeJsRpcFunction("canPasteNodesFromClipboard", nodeToMoveTo.get().getId());
        assertEquals("true", onSuccessResponse);
        onSuccessResponse = null;
        invokeJsRpcFunction("pasteNodesFromClipboard", nodeToMoveTo.get().getId());

        //when
        invokeJsRpcFunction("canPasteNodesFromClipboard", nodeToMoveTo.get().getId());

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsTrueForCorrectPaste() throws Exception {
        //given
        onSuccessResponse = null;
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .children(b2 -> b2
                                .node(randomNode).storeNode(nodeToMove1)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove2)
                                .node(randomNode)
                        )
                        .node(randomNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction("putNodeIdsToClipboard",
                Arrays.asList(nodeToMove1.get().getId(), nodeToMove2.get().getId())
        );
        invokeJsRpcFunction("canPasteNodesFromClipboard", null);

        //then
        assertEquals("true", onSuccessResponse);
    }
    @Test public void moveNodesToAnotherParent_movesNonRootNodesToNonTopParent() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove3 = new ObjectHolder<>();
        ObjectHolder<Node> prevParent = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMoveTo = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(prevParent)
                        .children(b2 -> b2
                                .node(randomNode).storeNode(nodeToMove1)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove2)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove3)
                        )
                        .node(randomNode).storeNode(nodeToMoveTo)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        //when
        invokeJsRpcFunction(
                "putNodeIdsToClipboard",
                Arrays.asList(nodeToMove1.get().getId(), nodeToMove2.get().getId(), nodeToMove3.get().getId())
        );
        invokeJsRpcFunction("pasteNodesFromClipboard", nodeToMoveTo.get().getId());

        //then
        prevParent.get().getChildNodes().removeIf(node ->
                nodeToMove1.get().getId().equals(node.getId())
                || nodeToMove2.get().getId().equals(node.getId())
                || nodeToMove3.get().getId().equals(node.getId())
        );
        nodeToMoveTo.get().getChildNodes().add(nodeToMove1.get());
        nodeToMoveTo.get().getChildNodes().add(nodeToMove2.get());
        nodeToMoveTo.get().getChildNodes().add(nodeToMove3.get());
        for (int i = 0; i < nodeToMoveTo.get().getChildNodes().size(); i++) {
            Node node = nodeToMoveTo.get().getChildNodes().get(i);
            node.setParentNode(nodeToMoveTo.get());
        }

        rootNodes.get().forEach(rootNode -> assertNodeInDatabase(jdbcTemplate, rootNode));
    }
    @Test public void moveNodesToAnotherParent_movesNonRootNodesToTop() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove3 = new ObjectHolder<>();
        ObjectHolder<Node> prevParent = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode).storeNode(prevParent)
                        .children(b2 -> b2
                                .node(randomNode).storeNode(nodeToMove1)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove2)
                                .node(randomNode)
                                .node(randomNode).storeNode(nodeToMove3)
                        )
                        .node(randomNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);

        //when
        invokeJsRpcFunction("putNodeIdsToClipboard", Arrays.asList(
                nodeToMove1.get().getId(),
                nodeToMove2.get().getId(),
                nodeToMove3.get().getId()
        ));
        invokeJsRpcFunction("pasteNodesFromClipboard", null);

        //then
        prevParent.get().getChildNodes().removeIf(node ->
                nodeToMove1.get().getId().equals(node.getId())
                || nodeToMove2.get().getId().equals(node.getId())
                || nodeToMove3.get().getId().equals(node.getId())
        );
        rootNodes.get().add(nodeToMove1.get());
        rootNodes.get().add(nodeToMove2.get());
        rootNodes.get().add(nodeToMove3.get());
        for (int i = 0; i < rootNodes.get().size(); i++) {
            Node node = rootNodes.get().get(i);
            node.setParentNode(null);
        }

        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void moveNodesToAnotherParent_movesRootNodesToNonTopParent() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove3 = new ObjectHolder<>();
        ObjectHolder<Node> newParent = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .children(b2 -> b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode).setName("newParent").storeNode(newParent)
                        .children(b2->b2
                                .node(randomNode).setName("existedNode1")
                                .node(randomNode).setName("existedNode2")
                        )
                        .node(randomNode)
                )
                .node(randomNode).setName("nodeToMove1").storeNode(nodeToMove1)
                .node(randomNode).setName("nodeToMove2").storeNode(nodeToMove2)
                .node(randomNode).setName("nodeToMove3").storeNode(nodeToMove3)
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);

        //when
        invokeJsRpcFunction(
                "putNodeIdsToClipboard",
                Arrays.asList(nodeToMove1.get().getId(), nodeToMove2.get().getId(), nodeToMove3.get().getId())
        );
        invokeJsRpcFunction("pasteNodesFromClipboard", newParent.get().getId());

        //then
        rootNodes.get().removeIf(node ->
                nodeToMove1.get().getId().equals(node.getId())
                || nodeToMove2.get().getId().equals(node.getId())
                || nodeToMove3.get().getId().equals(node.getId())
        );
        newParent.get().getChildNodes().add(nodeToMove1.get());
        newParent.get().getChildNodes().add(nodeToMove2.get());
        newParent.get().getChildNodes().add(nodeToMove3.get());
        for (int i = 0; i < newParent.get().getChildNodes().size(); i++) {
            Node node = newParent.get().getChildNodes().get(i);
            node.setParentNode(newParent.get());
        }
        assertNodeInDatabase(jdbcTemplate, rootNodes);
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

    private Pair<Node, Node> createAndSaveInnerNode(Consumer<Node> nodeModifier) {
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<Node> rootNode = new ObjectHolder<>();
        ObjectHolder<Node> innerNode = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeNode(rootNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode, nodeModifier).storeNode(innerNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        return Pair.of(rootNode.get(), innerNode.get());
    }

    private String invokeJsRpcFunction(String functionName, Object... args) throws ScriptException, NoSuchMethodException {
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
            return arg.getClass() == DoNotSerialize.class
                    ?((DoNotSerialize)arg).getValue().toString()
                    :writeValueAsString(objectMapper, arg);
        }
    }
}