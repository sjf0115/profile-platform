package com.data.engine.api.context;

import com.data.engine.api.DiRequestBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 同步任务上下文
 *
 * <p>作为业务层（profile-web）与同步引擎插件（DataX/SeaTunnel/...）之间的契约，
 * 不包含任何引擎私有概念，引擎插件通过 {@link DiRequestBuilder} 自行解析转换。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiContext {

    /** 业务任务 ID（用于日志、追踪） */
    private String jobId;

    /** 源端 */
    private Endpoint source;

    /** 目标端 */
    private Endpoint target;

    /** 并发通道数（DataX speed.channel 等同概念） */
    private int channel;

    /** 允许失败记录数 */
    private int errorRecord;

    /**
     * 端点描述（源/目标通用）。
     *
     * <p>config 是端点的中性参数全集，对平台不透明、对引擎插件透明：
     * 连接参数（数据源配置经归属插件翻译①归一化）与写入参数（导入场景平台策略 /
     * 导出场景投递表单参数）在组装侧合并，引擎插件按方言整体消费（翻译②）。</p>
     * <p>键约定：JDBC 系连接 host/port/database/username/password/properties；
     * 写入 tableName/writeMode/batchSize/targetColumn；文件 objectPath/format；消息 topic/messageKey。</p>
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Endpoint {

        /** 数据源类别：mysql / postgresql / clickhouse / kafka / ... */
        private String category;

        /** 中性参数全集（连接参数 + 写入参数，对平台不透明） */
        private Map<String, Object> config;

        /** 列名列表 */
        private List<String> columns;

        /** 写入前置 SQL 列表（仅 target 使用）：由引擎插件按方言生成/消费（如 upsert 先删后写） */
        private List<String> preSql;
    }
}
