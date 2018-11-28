package org.igye.outline.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeAttrValueRequest {
    private UUID objId;
    private String attrName;
    private Object value;
}
