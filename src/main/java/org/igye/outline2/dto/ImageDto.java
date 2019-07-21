package org.igye.outline2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ImageDto extends NodeDto {
    private UUID imgId;
}
