package org.igye.outline.exceptions;

public class OutlineException extends RuntimeException {
    public OutlineException(String message) {
        super(message);
    }

    public OutlineException(Throwable cause) {
        super(cause);
    }
}
