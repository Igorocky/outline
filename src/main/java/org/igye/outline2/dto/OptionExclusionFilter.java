package org.igye.outline2.dto;

import java.util.Optional;

public class OptionExclusionFilter {
    @Override
    public boolean equals(Object obj) {
        return obj != null && !((Optional)obj).isPresent();
    }
}
