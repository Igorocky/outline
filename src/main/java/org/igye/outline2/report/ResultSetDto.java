package org.igye.outline2.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultSetDto {
    private List<ColumnDto> columns;
    private List<Map<String, Object>> data = new ArrayList<>();
}
