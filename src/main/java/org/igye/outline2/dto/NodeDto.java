package org.igye.outline2.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NodeDto {
    // TODO: 22.07.2019 tc: fails on unknown attr
    private UUID id;
    private Optional<UUID> parentId = Optional.empty();
    private Optional<String> objectClass = Optional.empty();
    private Optional<String> name = Optional.empty();
    private Optional<List<NodeDto>> childNodes = Optional.empty();
    private Optional<UUID> icon = Optional.empty();
    @JsonIgnore
    private int ord;
    private Optional<UUID> imgId = Optional.empty();
    private Optional<String> text = Optional.empty();
}
