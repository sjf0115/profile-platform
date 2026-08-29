package com.data.engine.api.source;

import java.util.Map;

/**
 * 引擎表流式读取的行消费回调。
 *
 * <p>每行以 LinkedHashMap 传递（保持列顺序），由消费方按批处理后释放，避免全表加载。</p>
 */
@FunctionalInterface
public interface RowConsumer {

    /**
     * 消费单行数据。
     *
     * @param row 列名 -> 列值映射（保持列顺序）
     */
    void accept(Map<String, Object> row) throws Exception;
}
