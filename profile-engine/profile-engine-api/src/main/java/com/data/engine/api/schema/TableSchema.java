package com.data.engine.api.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 中性表结构定义。
 *
 * <p>由业务层（DatasetSyncService）从"数据源元数据 + 数据集字段定义 + 标签语义"组装，
 * 交给 {@link TableManager} 完成"建表 / Schema 演进"。</p>
 *
 * <p>引擎插件不应当感知该结构以外的业务概念（如 Dataset/Engine/Label）。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSchema {

    /** 数据库 / schema 名（可空，由 TableManager 自行决定是否使用） */
    private String database;

    /** 表名 */
    private String tableName;

    /** 表注释（可空） */
    private String comment;

    /** 列定义列表（保持业务期望的写入顺序） */
    private List<Column> columns;

    /** 排序键 / 主键列名（如 ClickHouse ORDER BY、MergeTree 主键） */
    private List<String> orderBy;

    /** 分区字段（引擎按需消费，可为表达式如 toYYYYMM(dt)） */
    private String partitionBy;

    /** 引擎扩展属性，用于承载方言差异（如 ClickHouse SETTINGS、Hive 表属性） */
    private java.util.Map<String, String> properties;

    public List<Column> getColumns() {
        return columns == null ? Collections.emptyList() : columns;
    }

    public List<String> getOrderBy() {
        return orderBy == null ? Collections.emptyList() : orderBy;
    }

    /** 便捷方法：按列名查找列定义。 */
    public Column findColumn(String name) {
        if (name == null || columns == null) {
            return null;
        }
        for (Column c : columns) {
            if (name.equalsIgnoreCase(c.getName())) {
                return c;
            }
        }
        return null;
    }

    /** 便捷方法：拷贝列名集合。 */
    public List<String> columnNames() {
        List<String> names = new ArrayList<>();
        if (columns != null) {
            for (Column c : columns) {
                names.add(c.getName());
            }
        }
        return names;
    }
}
