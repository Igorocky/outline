package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

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
        Node result = new Node();
        result.setId(null);
        if (id == null) {
            result.setChildNodes(nodeRepository.findByParentNodeIsNullOrderByOrd());
        } else {
            result = nodeRepository.findById(id).get();
        }
        return DtoConverter.toDto(result, depth);
    }

    @Transactional
    public NodeDto patchNode(NodeDto node) {
        if (node.getId() == null) {
            return createNewNode(node);
        } else {
            return patchExistingNode(node);
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
            node.setOrd(nodeRepository.findByParentNodeIsNullOrderByOrd().size());
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
