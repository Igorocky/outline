package org.igye.outline.common;

import org.igye.outline.exceptions.OutlineException;

public class OutlineUtils {
    public static <T> T accessDenied() {
        throw new OutlineException("Access denied.");
    }
}
