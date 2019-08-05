package org.igye.outline2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.igye.outline2.OutlineUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeDto {
    // TODO: 22.07.2019 tc: fails on unknown attr
    private UUID id;
    private Instant createdWhen;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptionalUuid.class)
    private Optional<UUID> parentId = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    private Optional<List<TagDto>> tags = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    private Optional<List<NodeDto>> childNodes = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private List<NodeDto> path;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Boolean canPaste;

    public List<String> getTagValues(UUID tagId) {
        if (!tags.isPresent()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (TagDto tag : tags.get()) {
            if (tagId.equals(tag.getTagId())) {
                result.add(tag.getValue());
            }
        }
        return result;
    }

    public String getTagSingleValue(UUID tagId) {
        return OutlineUtils.getSingleValue(getTagValues(tagId));
    }
}
