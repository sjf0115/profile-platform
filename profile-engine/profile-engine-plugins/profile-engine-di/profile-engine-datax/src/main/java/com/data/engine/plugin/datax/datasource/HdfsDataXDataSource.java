package com.data.engine.plugin.datax.datasource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * HDFS 数据源（DataX hdfsreader / hdfswriter 系）。
 *
 * <p>本期占位：抽象到位但配套的 {@code HdfsReaderBuilder / HdfsWriterBuilder} 暂不实装。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HdfsDataXDataSource implements DataXDataSource {

    /** category 固定为 "hdfs" */
    @Builder.Default
    private String category = "hdfs";

    /** 例如 hdfs://nameservice */
    private String defaultFS;
    /** Hadoop 配置（core-site / hdfs-site 关键键值） */
    private Map<String, String> hadoopConfig;
    /** Kerberos keytab 文件路径（可选） */
    private String kerberosKeytab;
    /** Kerberos principal（可选） */
    private String kerberosPrincipal;

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public Map<String, Object> toRawParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("category", category);
        params.put("defaultFS", defaultFS);
        params.put("hadoopConfig", hadoopConfig);
        params.put("kerberosKeytab", kerberosKeytab);
        params.put("kerberosPrincipal", kerberosPrincipal);
        return params;
    }
}
