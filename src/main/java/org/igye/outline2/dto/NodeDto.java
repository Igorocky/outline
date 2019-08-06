package org.igye.outline2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.pm.NodeClass;
import org.igye.outline2.pm.TagId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.getSingleValue;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeDto {
    // TODO: 22.07.2019 tc: fails on unknown attr
    private UUID id;
    private NodeClass clazz;
    private Instant createdWhen;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    private Optional<Map<TagId, List<TagValueDto>>> tags = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptionalUuid.class)
    private Optional<UUID> parentId = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    private Optional<List<NodeDto>> childNodes = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private List<NodeDto> path;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Boolean canPaste;

    public List<TagValueDto> getTagValues(TagId tagId) {
        return nullSafeGetter(
                tags,
                opt->opt.get(),
                map->map.get(tagId)
        );
    }

    public TagValueDto getTagSingleValue(TagId tagId) {
        return nullSafeGetter(
                tags,
                opt->opt.orElse(null),
                map-> getSingleValue(map.get(tagId))
        );
    }
}
