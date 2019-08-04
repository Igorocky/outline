package org.igye.outline2.manager;

import org.hibernate.Session;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.mapToSet;

@Component
public class NodeManager {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();
    @Autowired
    private Clipboard clipboard;

    @Transactional
    public NodeDto getNode(UUID id, Integer depth, Boolean includeCanPaste) {
        Node result;
        if (id == null) {
            result = new Node();
            result.setId(null);
            result.setChildNodes(nodeRepository.findByParentNodeId(null));
        } else {
            result = nodeRepository.findById(id).get();
        }
        NodeDto resultDto = DtoConverter.toDto(result, depth);
        resultDto.setPath(map(result.getPath(), DtoConverter::toPathElem));
        if (includeCanPaste) {
            resultDto.setCanPaste(validateMoveOfNodesFromClipboard(resultDto.getId()));
        }
        return resultDto;
    }

    @Transactional
    public NodeDto patchNode(NodeDto node) {
        if (node.getId() == null) {
            UUID id = createNewNode(node);
            return DtoConverter.toDto(nodeRepository.findById(id).get(), 0);
        } else {
            return patchExistingNode(node);
        }
    }

    @Transactional
    public void moveNodesFromClipboard(UUID to) {
        List<UUID> ids = clipboard.getNodeIds();
        if (!validateMoveOfNodesFromClipboard(to)) {
            throw new OutlineException("Invalid move request.");
        }
        List<Node> nodesToMove = ids.stream().map(nodeRepository::getOne).collect(Collectors.toList());
        nodesToMove.forEach(this::detachNodeFromParent);
        attachNodesToParent(to, nodesToMove);
        clipboard.setNodeIds(null);
    }

    @Transactional
    public boolean validateMoveOfNodesFromClipboard(UUID to) {
        List<UUID> ids = clipboard.getNodeIds();
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<Node> nodesToMove = ids.stream().map(nodeRepository::getOne).collect(Collectors.toList());
        return validateMove(nodesToMove, to);
    }

    @Transactional
    public void reorderNode(UUID id, int direction) {
        Node node = nodeRepository.findById(id).get();
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

    private boolean validateMove(List<Node> nodesToMove, UUID to) {
        if (to != null) {
            Set<UUID> parentPath = mapToSet(nodeRepository.getOne(to).getPath(), Node::getId);
            for (Node nodeToMove : nodesToMove) {
                if (parentPath.contains(nodeToMove.getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    private NodeDto patchExistingNode(NodeDto nodeDto) {
        Node node = nodeRepository.findById(nodeDto.getId()).get();
        updateNode(nodeDto, node);
        return DtoConverter.toDto(node, 0);
    }

    @Transactional
    public UUID createNewNode(NodeDto nodeDto) {
        Node node = null;
        if (DtoConverter.NODE.equals(nodeDto.getObjectClass().get())) {
            node = new Node();
        } else if (DtoConverter.IMAGE_NODE.equals(nodeDto.getObjectClass().get())) {
            node = new ImageRef();
        } else if (DtoConverter.TEXT_NODE.equals(nodeDto.getObjectClass().get())) {
            node = new Text();
        }
        updateNode(nodeDto, node);
        node.setCreatedWhen(clock.instant());
        if (nodeDto.getParentId() != null) {
            Node parent = nodeRepository.getOne(nodeDto.getParentId().get());
            parent.addChild(node);
        } else {
            nodeRepository.save(node);
        }
        entityManager.unwrap(Session.class).flush();
        return node.getId();
    }

    private void updateNode(NodeDto nodeDto, Node node) {
        if (nodeDto.getName() == null) {
            node.setName(null);
        } else if (nodeDto.getName().isPresent()) {
            node.setName(nodeDto.getName().get());
        }

        if (nodeDto.getIcon() == null) {
            node.setIcon(null);
        } else if (nodeDto.getIcon().isPresent()) {
            node.setIcon(imageRepository.findById(nodeDto.getIcon().get()).get());
        }

        if (node instanceof ImageRef) {
            ImageRef imageRef = (ImageRef) node;
            if (nodeDto.getImgId() == null) {
                imageRef.setImage(null);
            } else if (nodeDto.getImgId().isPresent()) {
                imageRef.setImage(imageRepository.findById(nodeDto.getImgId().get()).get());
            }
        }

        if (node instanceof Text) {
            Text text = (Text) node;
            if (nodeDto.getText() == null) {
                text.setText(null);
            } else if (nodeDto.getText().isPresent()) {
                text.setText(nodeDto.getText().get());
            }
        }
    }

    private void detachNodeFromParent(Node child) {
        if (child.getParentNode() != null) {
            child.getParentNode().detachChild(child);
        }
    }

    private void attachNodesToParent(UUID parentId, List<Node> children) {
        if (parentId != null) {
            Node parent = nodeRepository.getOne(parentId);
            children.forEach(parent::addChild);
        }
    }
}