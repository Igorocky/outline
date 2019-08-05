package org.igye.outline2.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TagDto {
    private UUID tagId;
    private String value;
}
