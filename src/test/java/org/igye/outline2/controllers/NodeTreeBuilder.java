package org.igye.outline2.controllers;

import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class NodeTreeBuilder {
    private List<Node> collectedNodes = new ArrayList<>();
    private Map<String, Object> savedObjects = new HashMap<>();

    public NodeTreeBuilder() {
    }

    public Map<String, Object> getSavedObjects() {
        return savedObjects;
    }

    public NodeTreeBuilder saveTree(String name) {
        return save(name, collectedNodes);
    }

    public NodeTreeBuilder save(String name) {
        return save(name, getLastNode());
    }

    public NodeTreeBuilder saveId(String name) {
        return save(name, getLastNode().getId());
    }

    private Node getLastNode() {
        return collectedNodes.get(collectedNodes.size()-1);
    }

    private NodeTreeBuilder save(String name, Object object) {
        if (savedObjects.containsKey(name)) {
            throw new OutlineException("Duplicated key.");
        }
        savedObjects.put(name, object);
        return this;
    }

    public NodeTreeBuilder node(Consumer<Node> ...setters) {
        Node node = new Node();
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
        childrenBuilder.savedObjects.forEach((k,v) -> save(k,v));
        return this;
    }
}
