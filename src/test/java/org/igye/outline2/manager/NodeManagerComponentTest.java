package org.igye.outline2.manager;

import org.igye.outline2.controllers.ControllerComponentTestBase;
import org.igye.outline2.controllers.NodeTreeBuilder;
import org.igye.outline2.controllers.ObjectHolder;
import org.igye.outline2.pm.Node;
import org.junit.Test;

import javax.script.ScriptException;
import java.util.List;
import java.util.function.Consumer;

import static org.igye.outline2.OutlineUtils.mapOf;
import static org.igye.outline2.common.Randoms.randomNode;
import static org.igye.outline2.controllers.OutlineTestUtils.assertNodeInDatabase;
import static org.igye.outline2.controllers.OutlineTestUtils.saveNodeTreeToDatabase;

public class NodeManagerComponentTest extends ControllerComponentTestBase {
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
}