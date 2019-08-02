package org.igye.outline2.manager;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Component
public class Clipboard {
    private List<UUID> nodeIds;
}
