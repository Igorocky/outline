package org.igye.outline2.manager;

import org.hibernate.Session;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClass;
import org.igye.outline2.pm.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.mapToSet;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;

@Component
public class NodeManager {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();
    @Autowired
    private Clipboard clipboard;

    @Transactional
    public NodeDto patchNode(NodeDto nodeDto) {
        Node node;
        if (nodeDto.getId() == null) {
            node = new Node();
            node.setClazz(nodeDto.getClazz());
            node.setCreatedWhen(clock.instant());
            nodeRepository.save(node);
        } else {
            node = nodeRepository.findById(nodeDto.getId()).get();
        }
        patchNode(nodeDto, node);
        entityManager.unwrap(Session.class).flush();
        return DtoConverter.toDto(node, 0);
    }

    @Transactional
    public NodeDto getNode(UUID id, Integer depth, Boolean includeCanPaste) {
        Node result;
        if (id == null) {
            result = new Node();
            result.setId(null);
            result.setClazz(NodeClass.TOP_CONTAINER);
            if (depth > 0) {
                result.setChildNodes(nodeRepository.findByParentNodeId(null));
            }
        } else {
            result = nodeRepository.getOne(id);
        }
        NodeDto resultDto = DtoConverter.toDto(result, depth);
        resultDto.setPath(map(result.getPath(), n -> DtoConverter.toDto(n, 0)));
        if (includeCanPaste) {
            resultDto.setCanPaste(validateMoveOfNodesFromClipboard(resultDto.getId()));
        }
        return resultDto;
    }

    @Transactional
    public boolean validateMoveOfNodesFromClipboard(UUID to) {
        List<UUID> ids = clipboard.getNodeIds();
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        return !nodeRepository.findAllById(ids).stream()
                .filter(nodeToMove -> !validateMoveToAnotherParent(to, nodeToMove))
                .findFirst()
                .isPresent();
    }

    @Transactional
    public void moveNodesFromClipboard(UUID to) {
        List<UUID> ids = clipboard.getNodeIds();
        if (CollectionUtils.isEmpty(ids)) {
            throw new OutlineException("Invalid move request.");
        }
        List<Node> nodesToMove = nodeRepository.findAllById(ids);
        nodesToMove.forEach(n -> moveNodeToAnotherParent(to, n));
        clipboard.setNodeIds(null);
    }

    @Transactional
    public void reorderNode(UUID id, int direction) {
        Node node = nodeRepository.getOne(id);
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            int oldIdx = -1;
            for (int i = 0; i < parentNode.getChildNodes().size(); i++) {
                Node child = parentNode.getChildNodes().get(i);
                if (child.getId().equals(node.getId())) {
                    oldIdx = i;
                    break;
                }
            }
            if (oldIdx < 0) {
                throw new OutlineException("oldIdx < 0");
            }
            int newIdx = oldIdx;
            if (direction == 1) {
                newIdx = 0;
            } else if (direction == 2) {
                newIdx = oldIdx - 1;
            } else if (direction == 3) {
                newIdx = oldIdx + 1;
            } else if (direction == 4) {
                newIdx = parentNode.getChildNodes().size()-1;
            }
            if (newIdx < 0) {
                newIdx = 0;
            } else if (newIdx >= parentNode.getChildNodes().size()) {
                newIdx = parentNode.getChildNodes().size()-1;
            }
            if (oldIdx != newIdx) {
                parentNode.getChildNodes().remove(oldIdx);
                parentNode.getChildNodes().add(newIdx, node);
            }
        }
    }

    private void patchNode(NodeDto nodeDto, Node node) {
        if (nodeDto.getParentId() == null || nodeDto.getParentId().isPresent()) {
            moveNodeToAnotherParent(
                    nullSafeGetter(nodeDto.getParentId(), opt->opt.get()),
                    node
            );
        }
        if (nodeDto.getTags().isPresent()) {
            updateTags(nodeDto, node);
        }
    }

    private void updateTags(NodeDto nodeDto, Node node) {
        nodeDto.getTags().get().forEach((tagId, tagValueDtos) -> {
            node.setTags(
                    tagId,
                    map(
                            tagValueDtos,
                            tagValueDto -> Tag.builder()
                                    .ref(nullSafeGetter(
                                            tagValueDto.getRef(),
                                            id -> nodeRepository.getOne(id)
                                    ))
                                    .value(tagValueDto.getValue())
                                    .build()
                    )
            );
        });
    }

    private void moveNodeToAnotherParent(UUID newParentId, Node node) {
        if (!Objects.equals(
                newParentId,
                nullSafeGetter(node.getParentNode(), p->p.getId())
        )) {
            if (!validateMoveToAnotherParent(newParentId, node)) {
                throw new OutlineException("Cannot move node " + node.getId() + " to parent " + newParentId);
            }
            Node oldParent = node.getParentNode();
            if (oldParent != null) {
                oldParent.detachChild(node);
            }
            if (newParentId != null) {
                Node newParent = nodeRepository.getOne(newParentId);
                newParent.addChild(node);
            }
        }
    }

    private boolean validateMoveToAnotherParent(UUID newParentId, Node nodeToMove) {
        if (newParentId != null) {
            Set<UUID> parentPath = mapToSet(nodeRepository.getOne(newParentId).getPath(), Node::getId);
            return parentPath.contains(nodeToMove.getId());
        }
        return true;
    }

}
