package org.igye.outline2.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColumnDto {
    private String name;
    private String title;
    private String componentName;
    private Map<String,?> componentConfig;
}
