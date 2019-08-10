package org.igye.outline2.manager;

import org.hibernate.Session;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.TagDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClass;
import org.igye.outline2.pm.Tag;
import org.igye.outline2.pm.TagId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.ifPresent;
import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.mapToMap;
import static org.igye.outline2.OutlineUtils.mapToSet;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;

@Component
public class NodeManager {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();
    @Autowired
    private Clipboard clipboard;

    @Transactional
    public UUID patchNode(NodeDto nodeDto) {
        Node node;
        if (nodeDto.getId() == null) {
            node = new Node();
            node.setClazz(nodeDto.getClazz());
            node.setCreatedWhen(clock.instant());
            node = nodeRepository.save(node);
        } else {
            node = nodeRepository.getOne(nodeDto.getId());
        }
        patchNode(nodeDto, node);
        return node.getId();
    }

    private void patchTag(Node node, TagDto tagDto) {
        Tag tag;
        if (tagDto.getId() == null) {
            tag = new Tag();
            tag.setId(UUID.randomUUID());
            tag.setTagId(tagDto.getTagId().getVal());
            tag.setValue(tagDto.getValue().getVal());
            node.addTag(tag);
        } else {
            tag = tagRepository.getOne(tagDto.getId());
            if (!tag.getNode().getId().equals(node.getId())) {
                throw new OutlineException("!tag.getNode().getId().equals(node.getId())");
            }
            Tag finalTag = tag;
            ifPresent(tagDto.getTagId(), tagId -> finalTag.setTagId(tagId));
            ifPresent(tagDto.getValue(), value -> {
                if (value != null) {
                    finalTag.setValue(value);
                } else {
                    finalTag.delete();
                }
            });
        }
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

    private boolean validateMoveOfNodesFromClipboard(UUID to) {
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
        Map<UUID, List<Node>> nodesToMove = mapToMap(nodeRepository.findAllById(ids),n->n.getId(),n->n);
        for (UUID idOfNodeToMove : ids) {
            moveNodeToAnotherParent(to, nodesToMove.get(idOfNodeToMove).get(0));
        }
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

    @Transactional
    public void setSingleTagForNode(UUID nodeId, TagId tagId, String value) {
        nodeRepository.getOne(nodeId).setTagSingleValue(tagId, value);
    }

    @Transactional
    public void removeTagsFromNode(UUID nodeId, TagId tagId) {
        nodeRepository.getOne(nodeId).removeTags(tagId);
    }

    private void patchNode(NodeDto nodeDto, Node node) {
        ifPresent(nodeDto.getParentId(), parId-> moveNodeToAnotherParent(parId, node));

        final List<TagDto> tags = nodeDto.getTags();
        if (tags != null) {
            tags.forEach(tagDto -> patchTag(node, tagDto));
        }
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
            return !parentPath.contains(nodeToMove.getId());
        }
        return true;
    }

}
