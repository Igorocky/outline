package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;

@Component
public class NodeManager {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();

    @Transactional
    public NodeDto getNode(UUID id, Integer depth) {
        Node result;
        if (id == null) {
            result = new Node();
            result.setId(null);
            result.setChildNodes(nodeRepository.findByParentNodeIdOrderByOrd(null));
        } else {
            result = nodeRepository.findById(id).get();
        }
        NodeDto resultDto = DtoConverter.toDto(result, depth);
        resultDto.setPath(DtoConverter.buildPathTo(result));
        return resultDto;
    }

    @Transactional
    public NodeDto patchNode(NodeDto node) {
        if (node.getId() == null) {
            return createNewNode(node);
        } else {
            return patchExistingNode(node);
        }
    }

    @Transactional
    public void reorderNode(UUID id, int direction) {
        Node node = nodeRepository.getOne(id);
        List<Node> nodes = new ArrayList<>(nodeRepository.findByParentNodeIdOrderByOrd(
                nullSafeGetter(node.getParentNode(), p->p.getId())
        ));
        int idx = 0;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(node.getId())) {
                idx = i;
                break;
            }
        }
        nodes.remove(idx);
        if (direction == 1) {
            idx = 0;
        } else if (direction == 2) {
            idx--;
        } else if (direction == 3) {
            idx++;
        } else if (direction == 4) {
            idx = nodes.size();
        }
        if (idx < 0) {
            idx = 0;
        } else if (idx > nodes.size()) {
            idx = nodes.size();
        }
        nodes.add(idx, node);
        updateOrder(nodes);
    }

    private void updateOrder(List<Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setOrd(i);
        }
    }

    private NodeDto patchExistingNode(NodeDto nodeDto) {
        Node node = nodeRepository.findById(nodeDto.getId()).get();
        updateNode(nodeDto, node);
        return DtoConverter.toDto(node, 0);
    }

    private NodeDto createNewNode(NodeDto nodeDto) {
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
            Node parent = nodeRepository.findById(nodeDto.getParentId().get()).get();
            parent.addChild(node);
            nodeRepository.save(parent);
        } else {
            node.setOrd(nodeRepository.findByParentNodeIdOrderByOrd(null).size());
            nodeRepository.save(node);
        }
        return DtoConverter.toDto(nodeRepository.findById(node.getId()).get(), 0);
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
}
