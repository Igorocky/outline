package org.igye.outline2.manager;

import org.igye.outline2.OutlineUtils;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Component
public class NodeManager {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();

    @Transactional
    public Node getNode(UUID id) {
        if (id == null) {
            List<NodeDto> childNodes = OutlineUtils.map(nodeRepository.findByParentNodeIsNullOrderByOrd(), n -> DtoConverter.toDto(n, 1));
        }
        return null;
    }
}
