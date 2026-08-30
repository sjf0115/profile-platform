package com.data.spi;

import java.util.Map;

/**
 * 同步配置翻译器。
 *
 * <p>职责：把本插件管辖的原始 config 归一化为同步链路（DI）消费的连接配置。
 * 字段名知识（如 user/username、database/schema 差异）收敛在归属插件，
 * 平台与 DI 引擎插件只面对统一契约，新增数据源类型无需改动平台与引擎侧。</p>
 *
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public interface DiConfigTranslator {

    /**
     * 归一化原始配置。
     * <p>JDBC 系约定产出字段：host / port / database / username / password / properties；
     * 非 JDBC 数据源（Kafka/MinIO 等）按自身语义归一化，由消费方引擎插件按 category 解释。</p>
     *
     * @param rawConfig 平台存储的原始配置（不为 null）
     * @return 归一化后的连接配置
     */
    Map<String, Object> translate(Map<String, Object> rawConfig);
}
