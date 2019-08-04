package org.igye.outline2.controllers;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.Clipboard;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.listOf;
import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.setOf;
import static org.igye.outline2.controllers.OutlineTestUtils.assertNodeInDatabase;
import static org.igye.outline2.controllers.OutlineTestUtils.saveNodeTreeToDatabase;
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
    private static final String ON_SUCCESS_CALLBACK = "function(response){Java.type('org.igye.outline2.controllers.BeControllerComponentTest').onSuccess(response)}";
    private static String actualGetUrl;
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
        MvcResult res = mvc.perform(
                get("/be/node?depth=1")
        )
                .andExpect(status().isOk())
                .andReturn();

        //then
        NodeDto nodeDto = parseNodeDto(res);
        assertEquals("ROOT_NODE", nodeDto.getObjectClass().get());
        assertTrue(nodeDto.getChildNodes().get().isEmpty());
        assertEquals(
                setOf("id", "parentId", "objectClass", "name", "icon", "childNodes", "path"),
                parseAsMap(res).keySet()
        );
    }
    @Test public void getNode_databaseIsEmptyAndDepthEq0_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
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
        assertEquals(setOf("id", "parentId", "objectClass", "name", "icon", "path"), parseAsMap(res).keySet());
    }
    @Test public void getNode_databaseIsEmptyAndDepthIsNotSpecified_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
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
        assertEquals(setOf("id", "parentId", "objectClass", "name", "icon", "path"), parseAsMap(res).keySet());
    }
    @Test public void getNode_nodeHas2LevelsOfChildrenAndDepth1Requested_onlyTheFirstLevelReturned() throws Exception {
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
    @Test public void getNode_nodeHas2LevelsOfChildrenAndDepth0Requested_noChildNodesAttributeReturned() throws Exception {
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
    @Test public void getNode_nodeHas2LevelsOfChildrenAndDepthIsNotRequested_noChildNodesAttributeReturned() throws Exception {
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
    @Test public void getNode_returnsNullForIdAndParentIdOfARootNode() throws Exception {
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
    @Test public void getNode_returnsAllRequiredAttributesForTheFakeTopNode() throws Exception {
        //given

        //when
        MvcResult res = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn();

        //then
        Map<String, Object> nodeDto = parseAsMap(res);
        assertNull(nodeDto.get("id"));
        assertEquals(setOf("parentId", "id", "objectClass", "name", "icon", "path"), parseAsMap(res).keySet());
    }
    @Test public void getNodeByIdInJs_nullIsPassed_rootNodeWithDepth1IsRequested() throws Exception {
        //given
        actualGetUrl = Randoms.string(10,20);

        //when
        invokeJsFunction("getNodeById", "[null]");

        //then
        assertEquals("/be/node?depth=1&includeCanPaste=true", actualGetUrl);
    }
    @Test public void getNodeByIdInJs_undefinedIsPassed_rootNodeWithDepth1IsRequested() throws Exception {
        //given
        actualGetUrl = Randoms.string(10,20);

        //when
        invokeJsFunction("getNodeById", "[undefined]");

        //then
        assertEquals("/be/node?depth=1&includeCanPaste=true", actualGetUrl);
    }
    @Test public void getNodeByIdInJs_UuidIsPassed_nodeWithTheGivenUuidWithDepth1IsRequested() throws Exception {
        //given
        actualGetUrl = Randoms.string(10,20);
        Node node = new Node();
        nodeRepository.save(node);

        //when
        invokeJsFunction("getNodeById", "['" + node.getId() + "']");

        //then
        assertEquals("/be/node/" + node.getId() + "?depth=1&includeCanPaste=true", actualGetUrl);
    }
    @Test public void patchNode_createsNewRootNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 9, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeId(null).stream()
                .map(Node::getId).collect(Collectors.toSet());
        String topFakeNode = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Node expectedNode = new Node();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildNode", "[" + topFakeNode + "]");

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
        String topFakeNode = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Text expectedNode = new Text();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildTextNode", "[" + topFakeNode + "]");

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
        String topFakeNode = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ImageRef expectedNode = new ImageRef();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildImageNode", "[" + topFakeNode + "]");

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
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        String currNodeStr = mvc.perform(
                get("/be/node/" + currNode.get().getId())
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new Node();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildNode", "[" + currNodeStr + "]");

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
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        String currNodeStr = mvc.perform(
                get("/be/node/" + currNode.get().getId())
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new Text();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildTextNode", "[" + currNodeStr + "]");

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
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        String currNodeStr = mvc.perform(
                get("/be/node/" + currNode.get().getId())
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Set<UUID> allNodeIdsBeforeTest = nodeRepository.findAll().stream().map(Node::getId).collect(Collectors.toSet());
        Node expectedNode = new ImageRef();
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildImageNode", "[" + currNodeStr + "]");

        //then
        UUID newNodeId = nodeRepository.findAll().stream()
                .map(Node::getId)
                .filter(id -> !allNodeIdsBeforeTest.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        currNode.get().addChild(expectedNode);
        assertNodeInDatabase(jdbcTemplate, currNode.get());
    }
    @Test public void patchNode_modifiesNameAttrFromNullToSomeStr() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(()->new Node(), node -> node.setName(null));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        final String expectedName = "adfdf asdf asdf asdf asdf";
        innerNode.setName(expectedName);

        //when
        invokeJsFunction("updateNodeName", "['" + innerNode.getId() + "','" + expectedName + "']");

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesNameAttrFromSomeStrToNull() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(()->new Node(), node -> node.setName("okjhhfafadfd"));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        System.out.println("Before:");
        printNode(rootNode);
        final String expectedName = null;
        innerNode.setName(expectedName);

        //when
        invokeJsFunction("updateNodeName", "['" + innerNode.getId() + "'," + expectedName + "]");

        //then
        System.out.println("After:");
        printNode(rootNode);
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesIconAttrFromNullToSomeUuid() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(()->new Node(), node -> node.setIcon(null));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        Image expectedIcon = Randoms.image();
        imageRepository.save(expectedIcon);
        innerNode.setIcon(expectedIcon);

        //when
        invokeJsFunction(
                "updateNodeIcon",
                "['" + innerNode.getId() + "','" + expectedIcon.getId() + "']"
        );

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesIconAttrFromSomeUuidToNull() throws Exception {
        //given
        Image existingIcon = Randoms.image();
        imageRepository.save(existingIcon);
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(()->new Node(), node -> node.setIcon(existingIcon));
        Node rootNode = existingNodes.getLeft();
        Node innerNode = existingNodes.getRight();
        Image expectedIcon = null;
        innerNode.setIcon(expectedIcon);

        //when
        invokeJsFunction(
                "updateNodeIcon",
                "['" + innerNode.getId() + "'," + expectedIcon + "]"
        );

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesTextAttrFromNullToSomeStr() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(()->new Text(), text -> ((Text)text).setText(null));
        Node rootNode = existingNodes.getLeft();
        Text innerNode = (Text) existingNodes.getRight();
        final String expectedText = "pldfnsxnbc agfsd f";
        innerNode.setText(expectedText);

        //when
        invokeJsFunction("updateTextNodeText", "['" + innerNode.getId() + "','" + expectedText + "']");

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesTextAttrFromSomeStrToNull() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(()->new Text(), text -> ((Text)text).setText("plkjj"));
        Node rootNode = existingNodes.getLeft();
        Text innerNode = (Text) existingNodes.getRight();
        final String expectedText = null;
        innerNode.setText(expectedText);

        //when
        invokeJsFunction("updateTextNodeText", "['" + innerNode.getId() + "'," + expectedText + "]");

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesImgAttrFromNullToSomeUuid() throws Exception {
        //given
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(()->new ImageRef(), image -> ((ImageRef)image).setImage(null));
        Node rootNode = existingNodes.getLeft();
        ImageRef innerNode = (ImageRef) existingNodes.getRight();
        final Image expectedImage = Randoms.image();
        imageRepository.save(expectedImage);
        innerNode.setImage(expectedImage);

        //when
        invokeJsFunction(
                "updateImageNodeImage",
                "['" + innerNode.getId() + "','" + expectedImage.getId() + "']"
        );

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void patchNode_modifiesImgAttrFromSomeUuidToNull() throws Exception {
        //given
        final Image existingImage = Randoms.image();
        imageRepository.save(existingImage);
        Pair<Node, Node> existingNodes = createAndSaveInnerNode(
                ()->new ImageRef(),
                image -> ((ImageRef)image).setImage(existingImage)
        );
        Node rootNode = existingNodes.getLeft();
        ImageRef innerNode = (ImageRef) existingNodes.getRight();
        final Image expectedImage = null;
        innerNode.setImage(expectedImage);

        //when
        invokeJsFunction(
                "updateImageNodeImage",
                "['" + innerNode.getId() + "'," + expectedImage + "]"
        );

        //then
        assertNodeInDatabase(jdbcTemplate, rootNode);
    }
    @Test public void reorderNode_movesStartNodeUp() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeUp", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeUp() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeUp", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeUp() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(randomNode)
                        .node(() -> new Text(), randomNode).storeNode(nodeToMove)
                )
                .node(randomNode)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);

        Node parent = nodeToMove.get().getParentNode();
        parent.getChildNodes().remove(4);
        parent.getChildNodes().add(3, nodeToMove.get());

        //when
        invokeJsFunction("moveNodeUp", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesStartNodeDown() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .children(b1->b1
                        .node(() -> new ImageRef(), randomNode).storeNode(nodeToMove)
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
        invokeJsFunction("moveNodeDown", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeDown() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeDown", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeDown() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeDown", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesStartNodeToStart() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToStart", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeToStart() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToStart", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeToStart() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToStart", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesStartNodeToEnd() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToEnd", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesMiddleNodeToEnd() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToEnd", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesEndNodeToEnd() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToEnd", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeUp() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeUp", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeDown() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeDown", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeToStart() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToStart", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void reorderNode_movesSingleNodeToEnd() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction("moveNodeToEnd", concatToArray("'" + nodeToMove.get().getId() + "'"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseWhenClipboardIsEmpty() throws Exception {
        //given
        onSuccessResponse = null;
        clipboard.setNodeIds(null);
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
        invokeJsFunction(
                "canPasteNodesFromClipboard",
                concatToArray(
                        "'" + nodeToMoveTo.get().getId() + "'",
                        ON_SUCCESS_CALLBACK
                )
        );

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseForPastingIntoOneOfNodesBeingMoved() throws Exception {
        //given
        onSuccessResponse = null;
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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

        String arrayOfIds = concatToArray(
                "'" + nodeToMove1.get().getId() + "'",
                "'" + nodeToMove2.get().getId() + "'",
                "'" + nodeToMove3.get().getId() + "'"
        );

        //when
        invokeJsFunction("putNodeIdsToClipboard", concatToArray(arrayOfIds));
        invokeJsFunction(
                "canPasteNodesFromClipboard",
                concatToArray(
                        "'" + nodeToMove2.get().getId() + "'",
                        ON_SUCCESS_CALLBACK
                )
        );

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseForPastingIntoChildNode() throws Exception {
        //given
        onSuccessResponse = null;
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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

        String arrayOfIds = concatToArray(
                "'" + nodeToMove1.get().getId() + "'",
                "'" + nodeToMove2.get().getId() + "'"
        );

        //when
        invokeJsFunction("putNodeIdsToClipboard", concatToArray(arrayOfIds));
        invokeJsFunction(
                "canPasteNodesFromClipboard",
                concatToArray(
                        "'" + nodeToMoveTo.get().getId() + "'",
                        ON_SUCCESS_CALLBACK
                )
        );

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsFalseAfterPasteWasDone() throws Exception {
        //given
        onSuccessResponse = null;
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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

        String arrayOfIds = concatToArray(
                "'" + nodeToMove1.get().getId() + "'",
                "'" + nodeToMove2.get().getId() + "'"
        );
        invokeJsFunction("putNodeIdsToClipboard", concatToArray(arrayOfIds));
        invokeJsFunction(
                "canPasteNodesFromClipboard",
                concatToArray("'" + nodeToMoveTo.get().getId() + "'", ON_SUCCESS_CALLBACK)
        );
        assertEquals("true", onSuccessResponse);
        onSuccessResponse = null;
        invokeJsFunction("pasteNodesFromClipboard", concatToArray("'" + nodeToMoveTo.get().getId() + "'"));

        //when
        invokeJsFunction(
                "canPasteNodesFromClipboard",
                concatToArray("'" + nodeToMoveTo.get().getId() + "'", ON_SUCCESS_CALLBACK)
        );

        //then
        assertEquals("false", onSuccessResponse);
    }
    @Test public void canPasteNodesFromClipboard_returnsTrueForCorrectPaste() throws Exception {
        //given
        onSuccessResponse = null;
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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

        String arrayOfIds = concatToArray(
                "'" + nodeToMove1.get().getId() + "'",
                "'" + nodeToMove2.get().getId() + "'"
        );

        //when
        invokeJsFunction("putNodeIdsToClipboard", concatToArray(arrayOfIds));
        invokeJsFunction(
                "canPasteNodesFromClipboard",
                concatToArray("null", ON_SUCCESS_CALLBACK)
        );

        //then
        assertEquals("true", onSuccessResponse);
    }
    @Test public void moveNodesToAnotherParent_movesNonRootNodesToNonTopParent() throws Exception {
        //given
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
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
                                .node(() -> new Text(), randomNode).storeNode(nodeToMove2)
                                .node(randomNode)
                                .node(() -> new ImageRef(), randomNode).storeNode(nodeToMove3)
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

        String arrayOfIds = concatToArray(
                "'" + nodeToMove1.get().getId() + "'",
                "'" + nodeToMove2.get().getId() + "'",
                "'" + nodeToMove3.get().getId() + "'"
        );

        //when
        invokeJsFunction("putNodeIdsToClipboard", concatToArray(arrayOfIds));
        invokeJsFunction("pasteNodesFromClipboard", concatToArray("'" + nodeToMoveTo.get().getId() + "'"));

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
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove3 = new ObjectHolder<>();
        ObjectHolder<Node> prevParent = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode, n->n.setName("root0"))
                .children(b1->b1
                        .node(randomNode, n->n.setName("prevParent")).storeNode(prevParent)
                        .children(b2 -> b2
                                .node(randomNode, n->n.setName("nodeToMove1")).storeNode(nodeToMove1)
                                .node(randomNode)
                                .node(randomNode, n->n.setName("nodeToMove2")).storeNode(nodeToMove2)
                                .node(randomNode)
                                .node(randomNode, n->n.setName("nodeToMove3")).storeNode(nodeToMove3)
                        )
                        .node(randomNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode, n->n.setName("root1"))
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);

        String arrayOfIds = concatToArray(
                "'" + nodeToMove1.get().getId() + "'",
                "'" + nodeToMove2.get().getId() + "'",
                "'" + nodeToMove3.get().getId() + "'"
        );

        //when
        invokeJsFunction("putNodeIdsToClipboard", concatToArray(arrayOfIds));
        invokeJsFunction("pasteNodesFromClipboard", concatToArray("null"));

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
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove1 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove2 = new ObjectHolder<>();
        ObjectHolder<Node> nodeToMove3 = new ObjectHolder<>();
        ObjectHolder<Node> newParent = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode, n->n.setName("root0"))
                .children(b1->b1
                        .node(randomNode)
                        .children(b2 -> b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode, n->n.setName("newParent")).storeNode(newParent)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode, n->n.setName("nodeToMove1")).storeNode(nodeToMove1)
                .node(randomNode, n->n.setName("nodeToMove2")).storeNode(nodeToMove2)
                .node(randomNode, n->n.setName("nodeToMove3")).storeNode(nodeToMove3)
                .node(randomNode, n->n.setName("root1"))
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);

        String arrayOfIds = concatToArray(
                "'" + nodeToMove1.get().getId() + "'",
                "'" + nodeToMove2.get().getId() + "'",
                "'" + nodeToMove3.get().getId() + "'"
        );

        //when
        invokeJsFunction("putNodeIdsToClipboard", concatToArray(arrayOfIds));
        invokeJsFunction("pasteNodesFromClipboard", concatToArray("'" + newParent.get().getId() + "'"));

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

    public static void doPatch(String url, String requestBody) throws Exception {
        mvc.perform(
                patch(url)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(requestBody)
        )
                .andExpect(status().isOk());
    }

    public static String doGet(String url) throws Exception {
        actualGetUrl = url;
        return mvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    public static void onSuccess(String response) throws Exception {
        onSuccessResponse = response;
    }

    private Pair<Node, Node> createAndSaveInnerNode(Supplier<Node> initialNode, Consumer<Node> nodeModifier) {
        List<Image> images = Randoms.list(3, Randoms::image);
        images.forEach(imageRepository::save);
        Consumer<Node> randomNode = randomNode(images);
        ObjectHolder<Node> rootNode = new ObjectHolder<>();
        ObjectHolder<Node> innerNode = new ObjectHolder<>();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode)
                .node(randomNode).storeNode(rootNode)
                .children(b1->b1
                        .node(randomNode)
                        .node(initialNode, randomNode.andThen(nodeModifier)).storeNode(innerNode)
                        .children(b2->b2
                                .node(randomNode)
                                .node(randomNode)
                                .node(randomNode)
                        )
                        .node(randomNode)
                )
                .node(randomNode)
        ;
        rootNodes.get().forEach(nodeRepository::save);
        return Pair.of(rootNode.get(), innerNode.get());
    }

    private void invokeJsFunction(String functionName, String arrOfArguments) throws ScriptException, NoSuchMethodException {
        jsAdapter.invokeFunction("doTestCall", functionName, arrOfArguments);
    }

    private String concatToArray(Object... elems) {
        return "[" + StringUtils.join(elems, ",") + "]";
    }
}