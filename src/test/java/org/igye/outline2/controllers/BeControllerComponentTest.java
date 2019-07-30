package org.igye.outline2.controllers;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
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
import java.util.function.Supplier;
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
    private static String actualGetUrl;

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
        nodeRepository.findByParentNodeIdOrderByOrd(null).forEach(nodeRepository::delete);
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
    @Test
    public void getNode_databaseIsEmptyAndDepthEq0_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeIdOrderByOrd(null).forEach(nodeRepository::delete);
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
    @Test
    public void getNode_databaseIsEmptyAndDepthIsNotSpecified_RootNodeWithoutChildrenListIsReturned() throws Exception {
        //given
        nodeRepository.findByParentNodeIdOrderByOrd(null).forEach(nodeRepository::delete);
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
        assertEquals(setOf("parentId", "id", "objectClass", "name", "icon", "path"), parseAsMap(res).keySet());
    }
    @Test
    public void getNodeByIdInJs_nullIsPassed_rootNodeWithDepth1IsRequested() throws Exception {
        //given
        actualGetUrl = Randoms.string(10,20);

        //when
        invokeJsFunction("getNodeById", "[null]");

        //then
        assertEquals("/be/node?depth=1", actualGetUrl);
    }
    @Test
    public void getNodeByIdInJs_undefinedIsPassed_rootNodeWithDepth1IsRequested() throws Exception {
        //given
        actualGetUrl = Randoms.string(10,20);

        //when
        invokeJsFunction("getNodeById", "[undefined]");

        //then
        assertEquals("/be/node?depth=1", actualGetUrl);
    }
    @Test
    public void getNodeByIdInJs_UuidIsPassed_nodeWithTheGivenUuidWithDepth1IsRequested() throws Exception {
        //given
        actualGetUrl = Randoms.string(10,20);

        //when
        invokeJsFunction("getNodeById", "['bbd97a77-87dc-4f3d-83e1-49a2659e76a0']");

        //then
        assertEquals("/be/node/bbd97a77-87dc-4f3d-83e1-49a2659e76a0?depth=1", actualGetUrl);
    }
    @Test
    public void patchNode_createsNewRootNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 9, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeIdOrderByOrd(null).stream()
                .map(Node::getId).collect(Collectors.toSet());
        String topFakeNode = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Node expectedNode = new Node();
        expectedNode.setOrd(existingNodes.size());
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildNode", "[" + topFakeNode + "]");

        //then
        UUID newNodeId = nodeRepository.findByParentNodeIdOrderByOrd(null).stream()
                .map(Node::getId)
                .filter(id -> !existingNodes.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        assertNodeInDatabase(jdbcTemplate, expectedNode);
    }
    @Test
    public void patchNode_createsNewRootTextNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 9, 10, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeIdOrderByOrd(null).stream()
                .map(Node::getId).collect(Collectors.toSet());
        String topFakeNode = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Text expectedNode = new Text();
        expectedNode.setOrd(existingNodes.size());
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildTextNode", "[" + topFakeNode + "]");

        //then
        UUID newNodeId = nodeRepository.findByParentNodeIdOrderByOrd(null).stream()
                .map(Node::getId)
                .filter(id -> !existingNodes.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        assertNodeInDatabase(jdbcTemplate, expectedNode);
    }
    @Test
    public void patchNode_createsNewRootImageNode() throws Exception {
        //given
        testClock.setFixedTime(2019, 7, 10, 10, 8, 11);
        Set<UUID> existingNodes = nodeRepository.findByParentNodeIdOrderByOrd(null).stream()
                .map(Node::getId).collect(Collectors.toSet());
        String topFakeNode = mvc.perform(
                get("/be/node")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ImageRef expectedNode = new ImageRef();
        expectedNode.setOrd(existingNodes.size());
        expectedNode.setCreatedWhen(testClock.instant());

        //when
        invokeJsFunction("createChildImageNode", "[" + topFakeNode + "]");

        //then
        UUID newNodeId = nodeRepository.findByParentNodeIdOrderByOrd(null).stream()
                .map(Node::getId)
                .filter(id -> !existingNodes.contains(id))
                .findFirst().get();
        expectedNode.setId(newNodeId);
        assertNodeInDatabase(jdbcTemplate, expectedNode);
    }
    @Test
    public void patchNode_createsNewInnerNode() throws Exception {
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
        expectedNode.setOrd(3);
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
    @Test
    public void patchNode_createsNewInnerTextNode() throws Exception {
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
        expectedNode.setOrd(3);
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
    @Test
    public void patchNode_createsNewInnerImageNode() throws Exception {
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
        Node expectedNode = new ImageRef();
        expectedNode.setOrd(3);
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
    @Test
    public void patchNode_modifiesNameAttrFromNullToSomeStr() throws Exception {
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
    @Test
    public void patchNode_modifiesNameAttrFromSomeStrToNull() throws Exception {
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
    @Test
    public void patchNode_modifiesIconAttrFromNullToSomeUuid() throws Exception {
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
    @Test
    public void patchNode_modifiesIconAttrFromSomeUuidToNull() throws Exception {
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
    @Test
    public void patchNode_modifiesTextAttrFromNullToSomeStr() throws Exception {
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
    @Test
    public void patchNode_modifiesTextAttrFromSomeStrToNull() throws Exception {
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
    @Test
    public void patchNode_modifiesImgAttrFromNullToSomeUuid() throws Exception {
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
    @Test
    public void patchNode_modifiesImgAttrFromSomeUuidToNull() throws Exception {
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

    public static void doPatch(String url, String requestBody) throws Exception {
        mvc.perform(
                patch(url)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(requestBody)
        )
                .andExpect(status().isOk());
    }

    public static void doGet(String url) {
        actualGetUrl = url;
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

    private Object invokeJsFunction(String functionName, String arrOfArguments) throws ScriptException, NoSuchMethodException {
        return jsAdapter.invokeFunction("doTestCall", functionName, arrOfArguments);
    }
}