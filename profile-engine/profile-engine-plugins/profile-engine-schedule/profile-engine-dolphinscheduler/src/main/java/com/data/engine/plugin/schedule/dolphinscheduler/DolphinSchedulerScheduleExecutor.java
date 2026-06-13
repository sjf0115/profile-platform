package com.data.engine.plugin.schedule.dolphinscheduler;

import com.data.engine.api.ScheduleExecutor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * DolphinScheduler 调度触发执行器。
 *
 * <p>实现 {@link ScheduleExecutor}，支持手动触发和状态查询。</p>
 */
@Slf4j
public class DolphinSchedulerScheduleExecutor implements ScheduleExecutor {

    private final DolphinSchedulerApiClient apiClient = new DolphinSchedulerApiClient();

    @Override
    public void init(Map<String, Object> config) {
        apiClient.init(config);
    }

    @Override
    public void trigger(String scheduleId) {
        try {
            String resp = apiClient.triggerWorkflow(scheduleId);
            log.info("DS 手动触发工作流: scheduleId={}, 响应={}", scheduleId, resp);
        } catch (IOException e) {
            log.error("DS 手动触发工作流失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 触发工作流失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStatus(String scheduleId) {
        try {
            return apiClient.getWorkflowStatus(scheduleId);
        } catch (IOException e) {
            log.error("DS 查询工作流状态失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 查询状态失败: " + e.getMessage(), e);
        }
    }
}
