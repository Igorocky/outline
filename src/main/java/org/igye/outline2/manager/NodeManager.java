package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.dto.TagDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.Tag;
import org.igye.outline2.rpc.Default;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.igye.outline2.common.OutlineUtils.ifPresent;
import static org.igye.outline2.common.OutlineUtils.map;
import static org.igye.outline2.common.OutlineUtils.mapOf;
import static org.igye.outline2.common.OutlineUtils.mapToMap;
import static org.igye.outline2.common.OutlineUtils.mapToSet;
import static org.igye.outline2.common.OutlineUtils.nullSafeGetter;

@RpcMethodsCollection
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

    @RpcMethod
    @Transactional
    public UUID rpcPatchNode(NodeDto nodeDto) {
        Node node;
        if (nodeDto.getId() == null) {
            node = new Node();
            node.setCreatedWhen(clock.instant());
            node = nodeRepository.save(node);
        } else {
            node = nodeRepository.getOne(nodeDto.getId());
        }
        patchNode(nodeDto, node);
        return node.getId();
    }

    @RpcMethod
    @Transactional
    public UUID rpcCreateNode(UUID parentId, String clazz) {
        return rpcPatchNode(NodeDto.builder().parentId(OptVal.of(parentId)).clazz(OptVal.of(clazz)).build());
    }

    @RpcMethod
    @Transactional
    public NodeDto rpcGetNode(@Default("null") UUID id,
                           @Default("0") Integer depth,
                           @Default("false") Boolean includePath,
                           @Default("false") Boolean includeCanPaste) {
        Node result;
        if (id == null) {
            result = new Node();
            result.setId(null);
            result.setClazz(NodeClasses.TOP_CONTAINER);
            if (depth > 0) {
                result.setChildNodes(nodeRepository.findByParentNodeId(null));
            }
        } else {
            result = nodeRepository.getOne(id);
        }
        NodeDto resultDto = DtoConverter.toDto(result, depth);
        if (includePath) {
            resultDto.setPath(map(result.getPath(), n -> DtoConverter.toDto(n, 0)));
        }
        if (includeCanPaste) {
            resultDto.setCanPaste(validateMoveOfNodesFromClipboard(resultDto.getId()));
        }
        return resultDto;
    }

    @RpcMethod
    @Transactional
    public void rpcPutNodeIdsToClipboard(List<UUID> ids) {
        clipboard.setNodeIds(ids);
    }

    @RpcMethod
    @Transactional
    public void rpcMoveNodesFromClipboard(UUID to) {
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

    @RpcMethod
    @Transactional
    public void rpcReorderNode(UUID nodeId, int direction) {
        Node node = nodeRepository.getOne(nodeId);
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

    @RpcMethod
    @Transactional
    public void rpcSetSingleTagForNode(UUID nodeId, String tagId, String value) {
        nodeRepository.getOne(nodeId).setTagSingleValue(tagId, value);
    }

    @RpcMethod
    @Transactional
    public void rpcRemoveTagsFromNode(UUID nodeId, String tagId) {
        nodeRepository.getOne(nodeId).removeTags(tagId);
    }

    @RpcMethod
    @Transactional
    public void rpcRemoveNode(UUID nodeId) {
        Node node = nodeRepository.getOne(nodeId);
        if (node.getParentNode() != null) {
            node.getParentNode().detachChild(node);
        }
        nodeRepository.deleteById(nodeId);
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
            ifPresent(tagDto.getValue(), value -> finalTag.setValue(value));
        }
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

    private void patchNode(NodeDto nodeDto, Node node) {
        ifPresent(nodeDto.getClazz(), clazz-> node.setClazz(clazz));
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
