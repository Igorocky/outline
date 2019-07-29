package org.igye.outline2.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptionalUuid.class)
    private Optional<UUID> parentId = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private List<PathElem> path;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    private Optional<String> objectClass = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptionalString.class)
    private Optional<String> name = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    private Optional<List<NodeDto>> childNodes = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptionalUuid.class)
    private Optional<UUID> icon = Optional.empty();

    @JsonIgnore
    private int ord;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptionalUuid.class)
    private Optional<UUID> imgId = Optional.empty();

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptionExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptionalString.class)
    private Optional<String> text = Optional.empty();
}
