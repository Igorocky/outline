package org.igye.outline2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class NodeDto implements HasTagDtos {
    // TODO: 22.07.2019 tc: fails on unknown attr
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    private UUID id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdWhen;

    private List<TagDto> tags;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptValExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptString.class)
    @JsonSerialize(using = SerializerOfOptString.class)
    private OptVal<String> clazz;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptValExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptUuid.class)
    @JsonSerialize(using = SerializerOfOptUuid.class)
    private OptVal<UUID> parentId = new OptVal<>();
    private List<NodeDto> childNodes;

    private List<NodeDto> path;
    private Boolean canPaste;
}
