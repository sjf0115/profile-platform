package com.data.engine.api;

import com.data.spi.SPI;

import java.util.Map;

/**
 * 调度任务注册/注销子产物。
 *
 * <p>负责在调度引擎中创建/更新/删除调度定义。
 * 与 {@link ScheduleEngineFactory#getTaskRegistrar()} 配合使用。</p>
 */
@SPI
public interface ScheduleTaskRegistrar {

    /**
     * 初始化（传入引擎配置）。
     * @param config 引擎配置（从 Engine 表 config 字段解析而来）
     */
    void init(Map<String, Object> config);

    /**
     * 注册调度任务，返回引擎侧的调度标识。
     * @param context 调度中性上下文
     * @return 引擎侧的调度标识（如 DolphinScheduler 的 workflowCode）
     */
    String register(ScheduleContext context);

    /**
     * 更新已注册的调度任务。
     * @param scheduleId 引擎侧的调度标识
     * @param context 调度中性上下文
     */
    void update(String scheduleId, ScheduleContext context);

    /**
     * 删除已注册的调度任务。
     * @param scheduleId 引擎侧的调度标识
     */
    void delete(String scheduleId);

    /**
     * 上线调度。
     * @param scheduleId 引擎侧的调度标识
     */
    void online(String scheduleId);

    /**
     * 下线调度。
     * @param scheduleId 引擎侧的调度标识
     */
    void offline(String scheduleId);
}
