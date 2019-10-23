package org.igye.outline2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TagDto {
    private UUID id;
    private UUID node;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptValExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptString.class)
    @JsonSerialize(using = SerializerOfOptString.class)
    private OptVal<String> tagId;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OptValExclusionFilter.class)
    @JsonDeserialize(using = DeserializerOfOptString.class)
    @JsonSerialize(using = SerializerOfOptString.class)
    private OptVal<String> value;
}
