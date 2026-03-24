package com.data.connector.api;

import com.data.connector.api.entity.ResultList;
import com.data.connector.api.entity.StructField;
import com.data.profile.common.enums.DataType;
import com.data.profile.common.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface Dialect {

    String getDriver();

    String getColumnPrefix();

    String getColumnSuffix();

    default Map<String,String> getDialectKeyMap() {
        return new HashMap<>();
    }

    List<String> getExcludeDatabases();

    default String getFullQualifiedTableName(String database, String schema, String table, boolean needQuote) {

        if (needQuote) {
            table = quoteIdentifier(table);

            if (!StringUtils.isEmptyOrNullStr(schema)) {
                table = quoteIdentifier(schema) + "." + table;
            }

            if (!StringUtils.isEmptyOrNullStr(database)) {
                table = quoteIdentifier(database) + "." + table;
            }

        } else {

            if (!StringUtils.isEmptyOrNullStr(schema)) {
                table = schema + "." + table;
            }

            if (!StringUtils.isEmptyOrNullStr(database)) {
                table = database + "." + table;
            }
        }

        return table;
    }

    default boolean invalidateItemCanOutput(){
        return true;
    }

    default boolean invalidateItemCanOutputToSelf(){
        return false;
    }

    default boolean supportToBeErrorDataStorage(){
        return false;
    }

    default String getJDBCType(DataType dataType){
        return dataType.toString();
    }

    default DataType getDataType(String jdbcType) {
        return DataType.valueOf(jdbcType);
    }

    default String quoteIdentifier(String entity) {
        return entity;
    }

    default String getQuoteIdentifier() {
        return "";
    }

    default String getTableExistsQuery(String table) {
        return String.format("SELECT 1 FROM %s WHERE 1=0", table);
    }

    default String getSchemaQuery(String table) {
        return String.format("SELECT * FROM %s WHERE 1=0", table);
    }

    default String getCountQuery(String table) {
        return String.format("SELECT COUNT(1) FROM %s", table);
    }

    default String getSelectQuery(String table) {
        return String.format("SELECT * FROM %s", table);
    }

    default String getCreateTableAsSelectStatement(String srcTable, String targetDatabase, String targetTable) {
        return String.format("CREATE TABLE %s.%s AS SELECT * FROM %s", quoteIdentifier(targetDatabase), quoteIdentifier(targetTable), quoteIdentifier(srcTable));
    }

    default String getCreateTableAsSelectStatementFromSql(String srcTable, String targetDatabase, String targetTable) {
        return String.format("CREATE TABLE %s.%s AS SELECT t.* FROM %s", quoteIdentifier(targetDatabase), quoteIdentifier(targetTable), srcTable);
    }

    default String getCreateTableStatement(String table, List<StructField> fields, TypeConverter typeConverter) {
        if (CollectionUtils.isNotEmpty(fields)) {
            String columns = fields.stream().map(field -> quoteIdentifier(field.getName()) + " " + typeConverter.convertToOriginType(field.getDataType()))
                    .collect(Collectors.joining(","));

            return String.format("CREATE TABLE IF NOT EXISTS %s (%s)", table, columns);
        }

        return "";
    }

    default String getInsertAsSelectStatement(String srcTable, String targetDatabase, String targetTable) {
        return String.format("INSERT INTO %s.%s SELECT * FROM %s", quoteIdentifier(targetDatabase), quoteIdentifier(targetTable), quoteIdentifier(srcTable));
    }

    default String getInsertAsSelectStatementFromSql(String srcTable, String targetDatabase, String targetTable) {
        return String.format("INSERT INTO %s.%s SELECT t.* FROM %s", quoteIdentifier(targetDatabase), quoteIdentifier(targetTable), srcTable);
    }

    String getErrorDataScript(Map<String, String> configMap);

    String getValidateResultDataScript(Map<String, String> configMap);

    ResultList getPageFromResultSet(Statement sourceConnectionStatement, ResultSet rs, String sourceTable, int start, int end) throws SQLException;
}
