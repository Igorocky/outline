package org.igye.outline2.dto;

public class OptValExclusionFilter {
    @Override
    public boolean equals(Object obj) {
        return obj != null && ((OptVal)obj).isAbsent();
    }
}
