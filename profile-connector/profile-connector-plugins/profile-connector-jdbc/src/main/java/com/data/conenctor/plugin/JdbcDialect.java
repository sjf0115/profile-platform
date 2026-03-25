package com.data.conenctor.plugin;

import com.data.connector.api.Dialect;
import com.data.connector.api.entity.ResultList;
import com.data.connector.api.utils.SqlUtils;
import com.data.profile.common.utils.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.data.profile.common.domain.connector.ConfigConstants.*;

public abstract class JdbcDialect implements Dialect {

    @Override
    public String getColumnPrefix() {
        return "`";
    }

    @Override
    public String getColumnSuffix() {
        return "`";
    }

    @Override
    public List<String> getExcludeDatabases() {
        return Arrays.asList("sys", "information_schema", "performance_schema", "mysql");
    }

    @Override
    public String getErrorDataScript(Map<String, String> configMap) {
        String errorDataFileName = configMap.get("error_data_file_name");
        if (StringUtils.isNotEmpty(errorDataFileName)) {
            if (StringUtils.isEmpty(configMap.get(ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE))) {
                return "select * from " + errorDataFileName;
            }
            return "select * from " + configMap.get(ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE) + "." + errorDataFileName;
        }
        return null;
    }

    @Override
    public String getValidateResultDataScript(Map<String, String> configMap) {
        String executionId = configMap.get("execution_id");
        if (StringUtils.isNotEmpty(executionId)) {
            return "select * from dv_job_execution_result where job_execution_id = " + executionId;
        }
        return null;
    }

    @Override
    public ResultList getPageFromResultSet(Statement sourceConnectionStatement, ResultSet rs, String sourceTable, int start, int end) throws SQLException {
        return SqlUtils.getPageFromResultSet(rs, start, end);
    }
}
