package org.igye.outline2.exceptions;

public class OutlineException extends RuntimeException {
    public OutlineException(String message) {
        super(message);
    }

    public OutlineException(Throwable cause) {
        super(cause);
    }
}
