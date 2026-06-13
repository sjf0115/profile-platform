package com.data.engine.api;

import com.data.profile.common.config.Config;
import com.data.spi.SPI;

/**
 * 调度引擎工厂 SPI 接口。
 *
 * <p>与 {@link AnalysisEngineFactory}、{@link DiEngineFactory} 同级，
 * 采用相同的单 SPI 多产物模式：</p>
 * <ul>
 *   <li>{@link #getTaskRegistrar()} 调度任务注册/注销</li>
 *   <li>{@link #getScheduler()} 调度触发/状态查询</li>
 * </ul>
 *
 * <p>通过 {@code PluginLoader.getPluginLoader(ScheduleEngineFactory.class).getOrCreatePlugin(category)}
 * 加载对应调度引擎插件（如 DolphinScheduler）。</p>
 */
@SPI
public interface ScheduleEngineFactory {

    /**
     * 引擎类别标识（如 "dolphinscheduler"），用于 SPI 路由。
     */
    String getCategory();

    void setConfig(Config config);

    Config getConfig();

    /**
     * 调度任务注册器。
     */
    ScheduleTaskRegistrar getTaskRegistrar();

    /**
     * 调度触发执行器。
     */
    ScheduleExecutor getScheduler();
}
