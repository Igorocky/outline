package org.igye.outline2.manager;

import org.apache.commons.lang3.NotImplementedException;
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
        return DtoConverter.toDto(result, depth, null);
    }

    @Transactional
    public NodeDto patchNode(NodeDto node) {
        if (node.getId() == null) {
            if (DtoConverter.ROOT_NODE.equals(node.getObjectClass().get())) {
                return patchRootNode(node);
            } else {
                return createNewNode(node);
            }
        } else {
            return patchExistingNode(node);
        }
    }

    private NodeDto patchExistingNode(NodeDto node) {
        throw new NotImplementedException("patchExistingNode");
    }

    private NodeDto createNewNode(NodeDto node) {
        throw new NotImplementedException("createNewNode");
    }

    private NodeDto patchRootNode(NodeDto node) {
        if (!node.getChildNodes().isPresent()) {
            return getNode(null, 0);
        }
//        Set<UUID> nodesToDelete = new HashSet<>();
//        Set<UUID> nodesToCreate = new HashSet<>();
//        Set<UUID> nodesToPatch = new HashSet<>();

        for (NodeDto childDto : node.getChildNodes().get()) {
            if (childDto.getId() == null) {

            }
//            nodeRepository.findById(childDto.ge)
        }
        return null;
    }
}
