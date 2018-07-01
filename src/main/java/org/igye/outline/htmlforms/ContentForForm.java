package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentForForm {
    public static final String IMAGE = "IMAGE";
    public static final String TEXT = "TEXT";

    private String type;
    private UUID id;
    private String text;
}
