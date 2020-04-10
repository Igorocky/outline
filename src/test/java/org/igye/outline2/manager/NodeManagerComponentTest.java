package org.igye.outline2.manager;

import org.igye.outline2.controllers.ControllerComponentTestBase;
import org.igye.outline2.controllers.NodeTreeBuilder;
import org.igye.outline2.controllers.ObjectHolder;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.Tag;
import org.igye.outline2.pm.TagIds;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.igye.outline2.common.OutlineUtils.mapOf;
import static org.igye.outline2.common.Randoms.randomNode;
import static org.igye.outline2.controllers.OutlineTestUtils.assertNodeInDatabase;
import static org.igye.outline2.controllers.OutlineTestUtils.saveNodeTreeToDatabase;

public class NodeManagerComponentTest extends ControllerComponentTestBase {
    @Autowired
    protected NodeManager nodeManager;

    @Test public void rpcPatchNode_its_possible_to_change_node_class_from_nonNull_to_nonNull() throws ScriptException, NoSuchMethodException {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode, n->{
                    n.setClazz("c1");
                    n.setTagSingleValue("tag1", "value1");
                    n.setTagSingleValue("tag2", "value2");
                }).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        node.get().setClazz("c2");

        //when
        invokeJsRpcFunction("patchNode", mapOf("id", node.get().getId(), "clazz", "c2"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcPatchNode_its_possible_to_change_node_class_from_nonNull_to_null() throws ScriptException, NoSuchMethodException {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode, n->{
                    n.setClazz("c1");
                    n.setTagSingleValue("tag1", "value1");
                    n.setTagSingleValue("tag2", "value2");
                }).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        node.get().setClazz(null);

        //when
        invokeJsRpcFunction("patchNode", mapOf("id", node.get().getId(), "clazz", null));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcPatchNode_its_possible_to_change_node_class_from_null_to_nonNull() throws ScriptException, NoSuchMethodException {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode, n->{
                    n.setClazz(null);
                    n.setTagSingleValue("tag1", "value1");
                    n.setTagSingleValue("tag2", "value2");
                }).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        node.get().setClazz("c2");

        //when
        invokeJsRpcFunction("patchNode", mapOf("id", node.get().getId(), "clazz", "c2"));

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcSetSingleTagForNode_addsNewTagWithNotNullValue() throws ScriptException, NoSuchMethodException {
        //given
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode()).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        Assert.assertTrue(node.get().getTags().isEmpty());
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        final String expectedTagId = "some-tag-id-776";
        final String expectedTagValue = "value-887766";
        node.get().getTags().add(Tag.builder().node(node.get()).tagId(expectedTagId).value(expectedTagValue).build());

        //when
        invokeJsRpcFunction("setSingleTagForNode", node.get().getId(), expectedTagId, expectedTagValue);

        //then
        doInTransactionV(session ->
                node.get().getTags().get(0).setId(nodeRepository.getOne(node.get().getId()).getTags().get(0).getId())
        );
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcSetSingleTagForNode_addsNewTagWithNullValue() throws ScriptException, NoSuchMethodException {
        //given
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode()).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        Assert.assertTrue(node.get().getTags().isEmpty());
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        final String expectedTagId = "some-tag-id-776";
        final String expectedTagValue = null;
        node.get().getTags().add(Tag.builder().node(node.get()).tagId(expectedTagId).value(expectedTagValue).build());

        //when
        invokeJsRpcFunction("setSingleTagForNode", node.get().getId(), expectedTagId, expectedTagValue);

        //then
        doInTransactionV(session ->
                node.get().getTags().get(0).setId(nodeRepository.getOne(node.get().getId()).getTags().get(0).getId())
        );
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcSetSingleTagForNode_overwritesExistingNotNullValueWithNotNullValue() throws ScriptException, NoSuchMethodException {
        //given
        final String expectedTagId = "some-tag-id-776";
        final String existingTagValue = "asdfasdf444";
        final String newTagValue = "aaaaa---bbbb";
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode(),n->n.addTag(
                        Tag.builder().id(UUID.randomUUID()).tagId(expectedTagId).value(existingTagValue).build()
                )).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        node.get().getTags().get(0).setValue(newTagValue);

        //when
        invokeJsRpcFunction("setSingleTagForNode", node.get().getId(), expectedTagId, newTagValue);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcSetSingleTagForNode_overwritesExistingNotNullValueWithNullValue() throws ScriptException, NoSuchMethodException {
        //given
        final String expectedTagId = "some-tag-id-776";
        final String existingTagValue = "asdfasdf444";
        final String newTagValue = null;
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode(),n->n.addTag(
                        Tag.builder().id(UUID.randomUUID()).tagId(expectedTagId).value(existingTagValue).build()
                )).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        node.get().getTags().get(0).setValue(newTagValue);

        //when
        invokeJsRpcFunction("setSingleTagForNode", node.get().getId(), expectedTagId, newTagValue);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcSetSingleTagForNode_overwritesExistingNullValueWithNotNullValue() throws ScriptException, NoSuchMethodException {
        //given
        final String expectedTagId = "some-tag-id-776";
        final String existingTagValue = null;
        final String newTagValue = "adasdf-asdfa-fasdf-----asd";
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> node = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode(),n->n.addTag(
                        Tag.builder().id(UUID.randomUUID()).tagId(expectedTagId).value(existingTagValue).build()
                )).storeNode(node)
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
        node.get().getTags().get(0).setValue(newTagValue);

        //when
        invokeJsRpcFunction("setSingleTagForNode", node.get().getId(), expectedTagId, newTagValue);

        //then
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
    @Test public void rpcRemoveNodeIconForNode_removesIconNodeAndTag() throws Exception {
        //given
        Consumer<Node> randomNode = randomNode();
        ObjectHolder<List<Node>> rootNodes = new ObjectHolder<>();
        ObjectHolder<Node> folder = new ObjectHolder<>();
        new NodeTreeBuilder().storeTree(rootNodes)
                .node(randomNode, n->n.setTagSingleValue(TagIds.NODE_ICON_IMG_ID, "img_id")).storeNode(folder)
                .children(b1->b1
                        .node(randomNode, n->n.setClazz(NodeClasses.NODE_ICON))
                        .node(randomNode)
                        .node(randomNode, n->n.setClazz(NodeClasses.NODE_ICON))
                )
        ;
        saveNodeTreeToDatabase(nodeRepository, rootNodes);
        assertNodeInDatabase(jdbcTemplate, rootNodes);

        //when
        nodeManager.rpcRemoveNodeIconForNode(folder.get().getId());

        //then
        folder.get().getChildNodes().removeIf(node ->
                NodeClasses.NODE_ICON.equals(node.getClazz())
        );
        folder.get().removeTags(TagIds.NODE_ICON_IMG_ID);
        assertNodeInDatabase(jdbcTemplate, rootNodes);
    }
}