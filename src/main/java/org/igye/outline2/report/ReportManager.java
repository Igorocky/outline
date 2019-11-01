package org.igye.outline2.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.igye.outline2.common.OutlineUtils;
import org.igye.outline2.rpc.Default;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.igye.outline2.common.OutlineUtils.UTC;

@Component
@RpcMethodsCollection
public class ReportManager {
    public static final String COLUMNS_CONFIG_BEGIN = "/*columns";
    public static final String COLUMNS_CONFIG_END = "columns*/";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper mapper;

    @RpcMethod
    @Transactional
    public ResultSetDto rpcRunReport(String name, @Default("{}") Map<String,Object> params) throws IOException {
        ReportConfig reportConfig = loadReportFromSqlFile("/reports/" + name + ".sql");
        final ResultSetDto resultSetDto = new ResultSetDto();
        resultSetDto.setColumns(reportConfig.getColumns());
        final List<Map<String, Object>> data = executeQuery(reportConfig.getSqlQuery(), params);
        if (resultSetDto.getColumns().isEmpty() && !data.isEmpty()) {
            for (String colName : data.get(0).keySet()) {
                resultSetDto.getColumns().add(ColumnDto.builder().name(colName).build());
            }
        }
        resultSetDto.setData(data);
        return resultSetDto;
    }

    private ReportConfig loadReportFromSqlFile(String filePath) throws IOException {
        String reportStr = OutlineUtils.readStringFromClasspath(filePath);
//        String reportStr = FileUtils.readFileToString(new File("D:/programs/java/outline2/src/main/resources" + filePath), StandardCharsets.UTF_8);
        String columnsConfigStr = reportStr.substring(
                reportStr.indexOf(COLUMNS_CONFIG_BEGIN) + COLUMNS_CONFIG_BEGIN.length(),
                reportStr.indexOf(COLUMNS_CONFIG_END)
        );
        String queryStr = reportStr.substring(0, reportStr.indexOf(COLUMNS_CONFIG_BEGIN));

        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setColumns(mapper.readValue(columnsConfigStr, new TypeReference<List<ColumnDto>>(){}));
        for (ColumnDto column : reportConfig.getColumns()) {
            column.setName(column.getName().toUpperCase());
        }
        reportConfig.setSqlQuery(queryStr);
        return reportConfig;
    }

    private List<Map<String, Object>> executeQuery(String query, Map<String, ?> params) {
        final ArrayList<Map<String, Object>> data = new ArrayList<>();
        final List<String> columns = new ArrayList<>();
        jdbcTemplate.query(query, params, resultSet -> {
            if (columns.isEmpty()) {
                final ResultSetMetaData metaData = resultSet.getMetaData();
                int colCnt = metaData.getColumnCount();
                for (int i = 1; i <= colCnt; i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
            }
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columns.size(); i++) {
                if (resultSet.getMetaData().getColumnType(i) == Types.TIMESTAMP) {
                    final Timestamp timestamp = resultSet.getTimestamp(i, Calendar.getInstance(UTC));
                    row.put(
                            columns.get(i-1),
                            timestamp==null?null:timestamp.getTime()
                    );
                } else {
                    row.put(columns.get(i-1), resultSet.getObject(i));
                }
            }
            data.add(row);
        });
        return data;
    }
}
