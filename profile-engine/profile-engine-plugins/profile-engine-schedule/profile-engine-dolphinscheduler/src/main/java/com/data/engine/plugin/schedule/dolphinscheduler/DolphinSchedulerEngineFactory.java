package com.data.engine.plugin.schedule.dolphinscheduler;

import com.data.engine.api.factory.ScheduleEngineFactory;
import com.data.engine.api.ScheduleExecutor;
import com.data.engine.api.ScheduleTaskRegistrar;
import com.data.profile.common.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * DolphinScheduler 调度引擎工厂。category = "dolphinscheduler"。
 */
@Slf4j
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

    @Override
    public Map<String, Object> testConnection(Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        DolphinSchedulerApiClient client = new DolphinSchedulerApiClient();
        client.init(config);
        long start = System.currentTimeMillis();
        try {
            String response = client.testConnection();
            long duration = System.currentTimeMillis() - start;
            result.put("connected", true);
            result.put("duration", duration);
            result.put("detail", response);
            log.info("DolphinScheduler 连通测试成功, 耗时={}ms", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            result.put("connected", false);
            result.put("duration", duration);
            result.put("error", e.getMessage());
            log.warn("DolphinScheduler 连通测试失败: {}", e.getMessage());
        }
        return result;
    }
}
