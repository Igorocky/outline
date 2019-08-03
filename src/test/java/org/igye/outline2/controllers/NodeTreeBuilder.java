package org.igye.outline2.controllers;

import org.igye.outline2.pm.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NodeTreeBuilder {
    private List<Node> collectedNodes = new ArrayList<>();

    public NodeTreeBuilder() {
    }

    public NodeTreeBuilder storeTree(ObjectHolder<List<Node>> holder) {
        holder.set(collectedNodes);
        return this;
    }

    public NodeTreeBuilder storeId(ObjectHolder<UUID> holder) {
        holder.set(getLastNode().getId());
        return this;
    }

    public NodeTreeBuilder storeNode(ObjectHolder<Node> holder) {
        holder.set(getLastNode());
        return this;
    }

    private Node getLastNode() {
        return collectedNodes.get(collectedNodes.size()-1);
    }

    public NodeTreeBuilder node(Consumer<Node> ...setters) {
        return node(() -> new Node(), setters);
    }

    public NodeTreeBuilder node(Supplier<Node> initialNode, Consumer<Node> ...setters) {
        Node node = initialNode.get();
        node.setId(UUID.randomUUID());
        for (Consumer<Node> setter : setters) {
            setter.accept(node);
        }
        collectedNodes.add(node);
        return this;
    }

    public NodeTreeBuilder children(Function<NodeTreeBuilder, NodeTreeBuilder> childrenBuilderFunction) {
        NodeTreeBuilder childrenBuilder = childrenBuilderFunction.apply(new NodeTreeBuilder());
        Node curNode = getLastNode();
        childrenBuilder.collectedNodes.forEach(curNode::addChild);
        return this;
    }
}
