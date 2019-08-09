package org.igye.outline2.manager;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.dto.TagValueDto;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Tag;
import org.springframework.util.CollectionUtils;

import java.util.Collections;

import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.mapToMap;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;

public class DtoConverter {

    public static NodeDto toDto(Node node, int depth) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setClazz(node.getClazz());
        nodeDto.setCreatedWhen(node.getCreatedWhen());
        nodeDto.setParentId(nullSafeGetter(
                node.getParentNode(),
                parentNode -> new OptVal<>(parentNode.getId())
        ));
        nodeDto.setTags(mapToMap(
                node.getTags(),
                tag -> tag.getTagId(),
                tag -> toTagValueDto(tag)
        ));


        if (depth > 0) {
            if (!CollectionUtils.isEmpty(node.getChildNodes())) {
                nodeDto.setChildNodes(map(node.getChildNodes(), n -> toDto(n,depth-1)));
            } else {
                nodeDto.setChildNodes(Collections.emptyList());
            }
        }

        return nodeDto;
    }

    public static TagValueDto toTagValueDto(Tag tagValue) {
        if (tagValue == null) {
            return null;
        }
        TagValueDto tagValueDto = new TagValueDto();
        tagValueDto.setRef(tagValue.getRef());
        tagValueDto.setValue(tagValue.getValue());
        return tagValueDto;
    }
}
