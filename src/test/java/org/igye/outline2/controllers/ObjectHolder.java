package org.igye.outline2.controllers;

public class ObjectHolder<T> {
    private T ref;

    public void set(T ref) {
        this.ref = ref;
    }

    public T get() {
        return ref;
    }
}
