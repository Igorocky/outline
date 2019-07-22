package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Component
public class NodeManager {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();

    @Transactional
    public NodeDto getNode(UUID id, Integer depth) {
        Node result = new Node();
        result.setId(null);
        if (id == null) {
            result.setChildNodes(nodeRepository.findByParentNodeIsNullOrderByOrd());
        } else {
            result = nodeRepository.findById(id).get();
        }
        return DtoConverter.toDto(result, depth);
    }
}
