package com.data.profile.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 引擎相关全局配置：分析/集成 引擎默认 category 与同步参数。
 三层兜底策略：Dataset 字段 > Engine 表 is_default(同 category) > 此处配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "profile.engine")
public class ProfileEngineConfig {

    /** 分析引擎类别（小写，等同 EngineFactory.getCategory()）：默认 ClickHouse */
    private String defaultAnalysisCategory = "clickhouse";

    /** 集成引擎类别（DataX/SeaTunnel/...）：默认 DataX */
    private String defaultDiCategory = "DataX";

    /** 同步并发通道数 */
    private int syncChannel = 3;

    /** 同步允许的最大错误记录数 */
    private int syncErrorRecord = 0;

    /** 单批写入条数 */
    private int syncBatchSize = 1000;

    /** 写入模式：insert / replace / update */
    private String syncWriteMode = "insert";
}
