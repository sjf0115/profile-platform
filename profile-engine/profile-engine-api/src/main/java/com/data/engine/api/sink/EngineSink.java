package com.data.engine.api.sink;

import com.data.engine.api.schema.Column;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 引擎数据写入 SPI（DML）。
 *
 * <p>与 {@link com.data.engine.api.catalog.EngineCatalog}（DDL）互补。
 * 实现方根据引擎特性生成最优写入语法
 *（如 ClickHouse 批量 VALUES、MySQL LOAD DATA）。</p>
 *
 * <p>本接口不做建表等 DDL 操作，表结构管理归 EngineCatalog。</p>
 */
public interface EngineSink {

    /**
     * 初始化连接配置（与 EngineCatalog 同源 config Map，含 host/port/database/...）。
     */
    void init(Map<String, Object> engineConfig) throws Exception;

    /**
     * 从 CSV 流导入数据到【已存在】的引擎表。
     *
     * <p>流程：解析 CSV（跳过 header）-> 分批写入 -> 返回累计写入行数。</p>
     * <p>不做建表（DDL 归 EngineCatalog），表不存在时抛异常。</p>
     *
     * @param tableName 目标表名
     * @param csvStream CSV 输入流（首行为 header）
     * @param columns   列定义（列名 + 中性类型，按 CSV 列顺序）
     * @return 累计写入行数
     */
    int importFromStream(String tableName, InputStream csvStream,
                         List<Column> columns) throws Exception;

    /**
     * 批量写入数据行。
     *
     * <p>契约：各行 Map 的 key 集合必须一致；列顺序取第一行 keySet；null 值写空字符串。</p>
     *
     * @param database  数据库名（可空，由实现决定默认值）
     * @param tableName 表名
     * @param rows      数据行，每行为 column_name -> value 的映射
     * @return 写入行数
     */
    int batchInsert(String database, String tableName,
                    List<Map<String, Object>> rows) throws Exception;

    /**
     * 统计表中记录数。
     */
    long count(String database, String tableName) throws Exception;
}
