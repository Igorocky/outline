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
    public enum ContentTypeForForm {
        IMAGE, TEXT
    }

    private ContentTypeForForm type;
    private UUID id;
    private String text;
}
