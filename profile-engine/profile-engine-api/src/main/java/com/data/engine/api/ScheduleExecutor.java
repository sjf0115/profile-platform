package com.data.engine.api;

import com.data.engine.api.factory.ScheduleEngineFactory;
import com.data.spi.SPI;

import java.util.Map;

/**
 * 调度触发执行器子产物。
 *
 * <p>用于手动触发、查询状态等操作。
 * 与 {@link ScheduleEngineFactory#getScheduler()} 配合使用。</p>
 */
@SPI
public interface ScheduleExecutor {

    /**
     * 初始化（传入引擎配置）。
     * @param config 引擎配置（从 Engine 表 config 字段解析而来）
     */
    void init(Map<String, Object> config);

    /**
     * 手动触发一次调度执行。
     * @param scheduleId 引擎侧的调度标识
     */
    void trigger(String scheduleId);

    /**
     * 查询调度状态。
     * @param scheduleId 引擎侧的调度标识
     * @return 调度状态描述
     */
    String getStatus(String scheduleId);
}
