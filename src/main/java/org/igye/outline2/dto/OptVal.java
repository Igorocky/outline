package org.igye.outline2.dto;

import java.util.function.Consumer;

public class OptVal<T> {
    public static final OptVal<Object> ABSENT_OPT_VAL = new OptVal<Object>() {
        @Override
        public boolean isPresent() {
            return false;
        }
    };
    private boolean isPresent = false;
    private T val;

    public OptVal() {
    }

    public OptVal(T val) {
        this.isPresent = true;
        this.val = val;
    }
    
    public void ifPresent(Consumer<T> consumer) {
        if (isPresent) {
            consumer.accept(val);
        }
    }

    public boolean isPresent() {
        return isPresent;
    }
    
    public boolean isAbsent() {
        return !isPresent();
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public T getVal() {
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }
}
