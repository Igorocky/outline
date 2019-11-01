package org.igye.outline2.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.igye.outline2.OutlineUtils;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Component
@RpcMethodsCollection
public class ReportManager {
    public static final String COLUMNS_CONFIG_BEGIN = "/*columns";
    public static final String COLUMNS_CONFIG_END = "columns*/";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper mapper;

    @RpcMethod
    @Transactional
    public ResultSetDto rpcRunReport(String name, Map<String,Object> params) throws IOException {
        Report report = loadReportFromSqlFile("/reports/" + name + ".sql");
        final ResultSetDto resultSetDto = new ResultSetDto();
        resultSetDto.setColumns(report.getColumns());
        resultSetDto.setData(executeQuery(report.getSqlQuery(), params));
        return resultSetDto;
    }

    private Report loadReportFromSqlFile(String filePath) throws IOException {
        String reportStr = OutlineUtils.readStringFromClasspath(filePath);
//        String reportStr = FileUtils.readFileToString(new File("D:/programs/java/outline2/src/main/resources" + filePath), StandardCharsets.UTF_8);
        String columnsConfigStr = reportStr.substring(
                reportStr.indexOf(COLUMNS_CONFIG_BEGIN) + COLUMNS_CONFIG_BEGIN.length(),
                reportStr.indexOf(COLUMNS_CONFIG_END)
        );
        String queryStr = reportStr.substring(0, reportStr.indexOf(COLUMNS_CONFIG_BEGIN));

        final Report report = new Report();
        report.setColumns(mapper.readValue(columnsConfigStr, new TypeReference<List<ColumnDto>>(){}));
        report.setSqlQuery(queryStr);
        return report;
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
