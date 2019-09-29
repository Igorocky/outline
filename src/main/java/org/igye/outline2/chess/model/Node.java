package org.igye.outline2.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {
    private Node<T> parent;
    private T value;
    private List<Node<T>> children = new ArrayList<>();

    public Node(Node<T> parent, T value) {
        this.parent = parent;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setChildren(List<Node<T>> children) {
        this.children = children;
    }

    public List<Node<T>> getChildren() {
        return children;
    }
}
