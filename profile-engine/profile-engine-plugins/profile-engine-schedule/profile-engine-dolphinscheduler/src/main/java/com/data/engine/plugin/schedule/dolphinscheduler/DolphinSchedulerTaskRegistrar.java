package com.data.engine.plugin.schedule.dolphinscheduler;

import com.data.engine.api.ScheduleContext;
import com.data.engine.api.ScheduleTaskRegistrar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

/**
 * DolphinScheduler 调度任务注册器。
 *
 * <p>实现 {@link ScheduleTaskRegistrar}，通过 DS OpenAPI 管理 Workflow 和 Schedule 生命周期。</p>
 */
@Slf4j
public class DolphinSchedulerTaskRegistrar implements ScheduleTaskRegistrar {

    private static final Gson gson = new GsonBuilder().create();

    private final DolphinSchedulerApiClient apiClient = new DolphinSchedulerApiClient();

    @Override
    public void init(Map<String, Object> config) {
        apiClient.init(config);
    }

    @Override
    public String register(ScheduleContext context) {
        try {
            // 1. 创建 Workflow（单节点 HTTP Task）
            String workflowJson = buildWorkflowJson(context);
            String createResp = apiClient.createWorkflow(workflowJson);
            log.info("DS 创建 Workflow 响应: {}", createResp);

            // 从响应中解析 workflowCode
            String workflowCode = parseCode(createResp, "code");
            if (workflowCode == null) {
                throw new RuntimeException("DS 创建 Workflow 未返回 code: " + createResp);
            }

            // 2. 创建 Schedule（如果有 Cron 表达式）
            if (context.getCronExpression() != null && !context.getCronExpression().isEmpty()) {
                String scheduleJson = buildScheduleJson(workflowCode, context.getCronExpression());
                String scheduleResp = apiClient.createSchedule(workflowCode, scheduleJson);
                log.info("DS 创建 Schedule 响应: {}", scheduleResp);

                // 3. 上线 Workflow 和 Schedule
                apiClient.onlineWorkflow(workflowCode);
                String scheduleId = parseCode(scheduleResp, "id");
                if (scheduleId != null) {
                    apiClient.onlineSchedule(scheduleId);
                }
            } else {
                // 无 Cron 表达式，仅上线 Workflow（手动触发模式）
                apiClient.onlineWorkflow(workflowCode);
            }

            return workflowCode;
        } catch (IOException e) {
            log.error("DS 注册调度任务失败: taskId={}", context.getTaskId(), e);
            throw new RuntimeException("DolphinScheduler 注册调度任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(String scheduleId, ScheduleContext context) {
        try {
            // 更新 Workflow
            String workflowJson = buildWorkflowJson(context);
            apiClient.updateWorkflow(scheduleId, workflowJson);

            // 重新上线
            apiClient.onlineWorkflow(scheduleId);
            log.info("DS 更新调度任务: scheduleId={}", scheduleId);
        } catch (IOException e) {
            log.error("DS 更新调度任务失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 更新调度任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String scheduleId) {
        try {
            apiClient.deleteWorkflow(scheduleId);
            log.info("DS 删除调度任务: scheduleId={}", scheduleId);
        } catch (IOException e) {
            log.error("DS 删除调度任务失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 删除调度任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void online(String scheduleId) {
        try {
            apiClient.onlineWorkflow(scheduleId);
            log.info("DS 上线调度: scheduleId={}", scheduleId);
        } catch (IOException e) {
            log.error("DS 上线调度失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 上线调度失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void offline(String scheduleId) {
        try {
            apiClient.offlineWorkflow(scheduleId);
            log.info("DS 下线调度: scheduleId={}", scheduleId);
        } catch (IOException e) {
            log.error("DS 下线调度失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 下线调度失败: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------
    // JSON 构建辅助方法
    // -----------------------------------------------------------------

    /** 构建单节点 HTTP Task 的 Workflow 定义 JSON */
    private String buildWorkflowJson(ScheduleContext context) {
        Map<String, Object> taskParams = new LinkedHashMap<>();
        taskParams.put("url", context.getCallbackUrl());
        taskParams.put("httpMethod", "GET");
        taskParams.put("httpParams", new ArrayList<>());
        taskParams.put("checkCondition", "STATUS_CODE_DEFAULT");
        taskParams.put("condition", "200");

        Map<String, Object> taskDef = new LinkedHashMap<>();
        taskDef.put("taskCode", "task_" + context.getTaskId());
        taskDef.put("taskName", context.getTaskName());
        taskDef.put("taskType", "HTTP");
        taskDef.put("taskParams", taskParams);
        taskDef.put("flag", "YES");

        List<Map<String, Object>> tasks = Collections.singletonList(taskDef);
        List<Map<String, Object>> edges = Collections.emptyList();

        Map<String, Object> workflow = new LinkedHashMap<>();
        workflow.put("name", context.getTaskName());
        workflow.put("description", context.getTaskName() + " 调度工作流");
        workflow.put("globalParams", "[]");
        workflow.put("locations", "[]");
        workflow.put("taskDefinitionJson", gson.toJson(tasks));
        workflow.put("taskRelationJson", gson.toJson(edges));
        workflow.put("timeout", 0);

        return gson.toJson(workflow);
    }

    /** 构建 Schedule JSON */
    private String buildScheduleJson(String workflowCode, String cronExpression) {
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("workflowCode", workflowCode);
        schedule.put("crontab", cronExpression);
        schedule.put("scheduleStatus", "ONLINE");
        return gson.toJson(schedule);
    }

    /** 从 JSON 响应中解析指定字段的值 */
    private String parseCode(String json, String field) {
        try {
            Map<?, ?> map = gson.fromJson(json, Map.class);
            if (map != null && map.containsKey("data")) {
                Object data = map.get("data");
                if (data instanceof Map) {
                    Object value = ((Map<?, ?>) data).get(field);
                    return value == null ? null : String.valueOf(value);
                }
            }
            // 顶层字段
            if (map != null && map.containsKey(field)) {
                Object value = map.get(field);
                return value == null ? null : String.valueOf(value);
            }
        } catch (Exception e) {
            log.warn("解析 DS 响应失败: field={}, json={}", field, json, e);
        }
        return null;
    }
}
