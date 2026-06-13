package com.data.engine.plugin.schedule.dolphinscheduler;

import com.data.engine.api.ScheduleContext;
import com.data.engine.api.ScheduleEngineFactory;
import com.data.engine.api.ScheduleExecutor;
import com.data.engine.api.ScheduleTaskRegistrar;
import com.data.profile.common.config.Config;

/**
 * DolphinScheduler 调度引擎工厂。category = "dolphinscheduler"。
 */
public class DolphinSchedulerEngineFactory implements ScheduleEngineFactory {

    public static final String CATEGORY = "dolphinscheduler";

    private Config config;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public ScheduleTaskRegistrar getTaskRegistrar() {
        return new DolphinSchedulerTaskRegistrar();
    }

    @Override
    public ScheduleExecutor getScheduler() {
        return new DolphinSchedulerScheduleExecutor();
    }
}
