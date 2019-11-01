package org.igye.outline2.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.Clipboard;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.TagIds;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.igye.outline2.common.OutlineUtils.listOf;
import static org.igye.outline2.common.OutlineUtils.map;
import static org.igye.outline2.common.OutlineUtils.mapOf;
import static org.igye.outline2.common.OutlineUtils.setOf;
import static org.igye.outline2.common.Randoms.randomNode;
import static org.igye.outline2.controllers.OutlineTestUtils.assertMapsEqual;
import static org.igye.outline2.controllers.OutlineTestUtils.assertNodeInDatabase;
import static org.igye.outline2.controllers.OutlineTestUtils.doNotSerialize;
import static org.igye.outline2.controllers.OutlineTestUtils.saveNodeTreeToDatabase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BeControllerComponentTest extends ControllerComponentTestBase {
    @Autowired
    private Clipboard clipboard;

    @Test public void getNode_databaseIsEmptyAndDepthEq1_RootNodeWithEmptyChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        String res = invokeJsRpcFunction("getNode", mapOf("depth", 1));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals(NodeClasses.TOP_CONTAINER, nodeDto.getClazz().getVal());
        assertTrue(nodeDto.getChildNodes().isEmpty());
        assertEquals(
                setOf("id", "clazz", "tags", "parentId", "childNodes"),
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
        assertEquals(NodeClasses.TOP_CONTAINER, nodeDto.getClazz().getVal());
        assertNull(nodeDto.getChildNodes());
        assertEquals(setOf("id", "clazz", "tags", "parentId"), parseAsMap(res).keySet());
    }
    @Test public void getNode_databaseIsEmptyAndDepthIsNotSpecified_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
        assertTrue(nodeRepository.findAll().isEmpty());

        //when
        String res = invokeJsRpcFunction("getNode", Collections.emptyMap());

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals(NodeClasses.TOP_CONTAINER, nodeDto.getClazz().getVal());
        assertNull(nodeDto.getChildNodes());
        assertEquals(setOf("id", "clazz", "tags", "parentId"), parseAsMap(res).keySet());
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
        assertEquals(setOf("id", "clazz", "tags", "parentId"), parseAsMap(res).keySet());
    }
    @Test public void getNodeInJs_nullIdIsPassed_rootNodeIsRequested() throws Exception {
        //given
        actualPatchUrl = null;
        actualPatchBody = null;

        //when
        invokeJsRpcFunction("getNode", mapOf("id", null));

        //then
        assertEquals("/be/rpc/rpcGetNode", actualPatchUrl);
        assertMapsEqual(
                mapOf("id", null),
                parseAsMap(actualPatchBody)
        );
    }
    @Test public void getNodeInJs_undefinedIdIsPassed_rootNodeIsRequested() throws Exception {
        //given
        actualPatchUrl = null;
        actualPatchBody = null;

        //when
        invokeJsRpcFunction("getNode", Collections.emptyMap());

        //then
        assertEquals("/be/rpc/rpcGetNode", actualPatchUrl);
        assertMapsEqual(
                Collections.emptyMap(),
                parseAsMap(actualPatchBody)
        );
    }
    @Test public void getNodeInJs_UuidIsPassed_nodeWithTheGivenUuidIsRequested() throws Exception {
        //given
        actualPatchUrl = null;
        actualPatchBody = null;
        Node node = new Node();
        nodeRepository.save(node);

        //when
        invokeJsRpcFunction("getNode", mapOf(
                "id", node.getId()
        ));

        //then
        assertEquals("/be/rpc/rpcGetNode", actualPatchUrl);
        assertMapsEqual(
                mapOf("id", node.getId().toString()),
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
        invokeJsRpcFunction("createChildNode", doNotSerialize(topFakeNode), NodeClasses.CONTAINER);

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
        final String expectedText = "some-text-qgfwfg";
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClasses.TEXT);
        expectedNode.setCreatedWhen(testClock.instant());
        expectedNode.setTagSingleValue(TagIds.TEXT, expectedText);

        //when
        invokeJsRpcFunction("createChildTextNode", doNotSerialize(topFakeNode), expectedText);

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
        String topNode = invokeJsRpcFunction("getNode", null);
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClasses.IMAGE);
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
        String currNodeStr = invokeJsRpcFunction("getNode", mapOf(
                "id", currNode.get().getId()
        ));
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new Node();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsRpcFunction("createChildNode", doNotSerialize(currNodeStr), NodeClasses.CONTAINER);

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
        String currNodeStr = invokeJsRpcFunction("getNode", mapOf(
                "id", currNode.get().getId()
        ));
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        final String expectedText = "some-text-qgfwfg";
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClasses.TEXT);
        expectedNode.setCreatedWhen(testClock.instant());
        expectedNode.setTagSingleValue(TagIds.TEXT, expectedText);

        //when
        invokeJsRpcFunction("createChildTextNode", doNotSerialize(currNodeStr), expectedText);

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
        String currNodeStr = invokeJsRpcFunction("getNode", mapOf(
                "id", currNode.get().getId()
        ));
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new Node();
        expectedNode.setClazz(NodeClasses.IMAGE);
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
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(node -> node.removeTags(TagIds.NAME));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        final String expectedName = "adfdf asdf asdf asdf asdf";
        innerNode.setTagSingleValue(TagIds.NAME, expectedName);

        //when
        invokeJsRpcFunction("updateNodeName", innerNode.getId(), expectedName);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesNameTagFromSomeStrToNull() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(
                node -> node.setTagSingleValue(TagIds.NAME, "okjhhfafadfd")
        );
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        innerNode.removeTags(TagIds.NAME);

        //when
        invokeJsRpcFunction("updateNodeName", innerNode.getId(), null);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesIconTagFromNullToSomeUuid() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(node -> node.removeTags(TagIds.ICON));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        UUID expectedIcon = UUID.randomUUID();
        innerNode.setTagSingleValue(TagIds.ICON, expectedIcon.toString());

        //when
        invokeJsRpcFunction("updateNodeIcon", innerNode.getId(), expectedIcon);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesIconTagFromSomeUuidToNull() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(
                node -> node.setTagSingleValue(TagIds.ICON, UUID.randomUUID().toString())
        );
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        innerNode.removeTags(TagIds.ICON);

        //when
        invokeJsRpcFunction("updateNodeIcon", innerNode.getId(), null);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesTextTagFromNullToSomeStr() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(n -> n.removeTags(TagIds.TEXT));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        final String expectedText = "pldfnsxnbc agfsd f";
        innerNode.setTagSingleValue(TagIds.TEXT, expectedText);

        //when
        invokeJsRpcFunction("updateTextNodeText", innerNode.getId(), expectedText);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesTextTagFromSomeStrToNull() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(n -> n.setTagSingleValue(TagIds.TEXT, "plkjj"));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        innerNode.removeTags(TagIds.TEXT);

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
        String res = invokeJsRpcFunction("getNode", mapOf(
                "id", nodeToMoveTo.get().getId(),
                "includeCanPaste", true
        ));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertFalse(nodeDto.getCanPaste());
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
        String res = invokeJsRpcFunction("getNode", mapOf(
                "id", nodeToMove2.get().getId(),
                "includeCanPaste", true
        ));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertFalse(nodeDto.getCanPaste());
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
        String res = invokeJsRpcFunction("getNode", mapOf(
                "id", nodeToMoveTo.get().getId(),
                "includeCanPaste", true
        ));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertFalse(nodeDto.getCanPaste());
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
        String res = invokeJsRpcFunction("getNode", mapOf(
                "id", nodeToMoveTo.get().getId(),
                "includeCanPaste", true
        ));
        NodeDto nodeDto = parseNodeDto(res);
        assertTrue(nodeDto.getCanPaste());
        onSuccessResponse = null;

        invokeJsRpcFunction("pasteNodesFromClipboard", nodeToMoveTo.get().getId());

        //when
        res = invokeJsRpcFunction("getNode", mapOf(
                "id", nodeToMoveTo.get().getId(),
                "includeCanPaste", true
        ));

        //then
        nodeDto = parseNodeDto(res);
        assertFalse(nodeDto.getCanPaste());
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
        String res = invokeJsRpcFunction("getNode", mapOf(
                "id", null,
                "includeCanPaste", true
        ));

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertTrue(nodeDto.getCanPaste());
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


}