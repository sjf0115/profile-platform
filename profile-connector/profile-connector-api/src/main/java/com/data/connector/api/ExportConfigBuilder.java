package com.data.connector.api;

/**
 * 投递配置表单构建器
 * <p>各 Connector 通过此接口声明投递配置表单字段，前端根据返回的 PluginParam[] JSON 动态渲染。</p>
 * <p>返回 null 表示该数据源类型无需额外投递配置（如 Kafka）。</p>
 *
 * 作者：SmartSi
 */
public interface ExportConfigBuilder {

    /**
     * 构建投递配置表单
     *
     * @return PluginParam[] JSON 字符串
     */
    String build();
}
