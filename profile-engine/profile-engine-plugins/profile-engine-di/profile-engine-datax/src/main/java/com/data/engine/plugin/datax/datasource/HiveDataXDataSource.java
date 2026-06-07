package com.data.engine.plugin.datax.datasource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Hive 数据源（基于 HDFS + Hive Metastore）。DataX 中通常走 hdfsreader/hdfswriter，
 * 路径由 metastore 解析后传入 path/fileType 等。
 *
 * <p>本期占位：抽象到位但配套的 {@code HiveReaderBuilder / HiveWriterBuilder} 暂不实装。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HiveDataXDataSource implements DataXDataSource {

    /** category 固定为 "hive" */
    @Builder.Default
    private String category = "hive";

    /** 底层 HDFS 配置 */
    private HdfsDataXDataSource hdfs;
    /** thrift://metastore-host:9083 */
    private String metastoreUri;
    /** 数据库名 */
    private String database;
    /** 表 HDFS 物理路径（可由 metastore 查询得到，调用方负责注入） */
    private String tableLocation;

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public Map<String, Object> toRawParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("category", category);
        params.put("metastoreUri", metastoreUri);
        params.put("database", database);
        params.put("tableLocation", tableLocation);
        if (hdfs != null) {
            params.put("hdfs", hdfs.toRawParams());
        }
        return params;
    }
}
