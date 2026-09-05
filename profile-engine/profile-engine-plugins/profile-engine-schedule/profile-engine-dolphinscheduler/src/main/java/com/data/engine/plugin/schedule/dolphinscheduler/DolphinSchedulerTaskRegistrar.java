package com.data.engine.plugin.schedule.dolphinscheduler;

import com.data.engine.api.context.ScheduleContext;
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
 * <p>API 参考：/dolphinscheduler/swagger-ui/index.html#/process%20definition%20related%20operation</p>
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
            Map<String, String> params = buildWorkflowParams(context);
            String createResp = apiClient.createWorkflow(params);
            log.info("DS 创建 Workflow 响应: {}", createResp);

            // 从响应中解析 workflowCode
            String workflowCode = parseCode(createResp, "code");
            if (workflowCode == null) {
                throw new RuntimeException("DS 创建 Workflow 未返回 code: " + createResp);
            }

            // 2. 创建 Schedule（如果有 Cron 表达式）
            if (context.getCronExpression() != null && !context.getCronExpression().isEmpty()) {
                String scheduleJson = buildScheduleCronJson(context.getCronExpression());
                String scheduleResp = apiClient.createSchedule(workflowCode, scheduleJson);
                log.info("DS 创建 Schedule 响应: {}", scheduleResp);

                // 3. 上线 Workflow 和 Schedule
                apiClient.onlineWorkflow(workflowCode, context.getTaskName());
                String scheduleId = parseCode(scheduleResp, "id");
                if (scheduleId != null) {
                    apiClient.onlineSchedule(scheduleId);
                }
            } else {
                // 无 Cron 表达式，仅上线 Workflow（手动触发模式）
                apiClient.onlineWorkflow(workflowCode, context.getTaskName());
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
            Map<String, String> params = buildWorkflowParams(context);
            apiClient.updateWorkflow(scheduleId, params);

            // 重新上线
            apiClient.onlineWorkflow(scheduleId, context.getTaskName());
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
            // 查询 workflow 名称后再上线
            String statusResp = apiClient.getWorkflowStatus(scheduleId);
            String workflowName = parseWorkflowName(statusResp);
            apiClient.onlineWorkflow(scheduleId, workflowName != null ? workflowName : "workflow");
            log.info("DS 上线调度: scheduleId={}", scheduleId);
        } catch (IOException e) {
            log.error("DS 上线调度失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 上线调度失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void offline(String scheduleId) {
        try {
            String statusResp = apiClient.getWorkflowStatus(scheduleId);
            String workflowName = parseWorkflowName(statusResp);
            apiClient.offlineWorkflow(scheduleId, workflowName != null ? workflowName : "workflow");
            log.info("DS 下线调度: scheduleId={}", scheduleId);
        } catch (IOException e) {
            log.error("DS 下线调度失败: scheduleId={}", scheduleId, e);
            throw new RuntimeException("DolphinScheduler 下线调度失败: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------
    // 参数构建辅助方法
    // -----------------------------------------------------------------

    /** 构建单节点 HTTP Task 的 Workflow 定义参数（表单形式） */
    private Map<String, String> buildWorkflowParams(ScheduleContext context) throws IOException {
        // 1. 从 DS 生成有效的 task code
        long taskCode = generateTaskCode();
        log.info("DS 生成 task code: {}", taskCode);

        // 2. 构建 HTTP Task 参数
        Map<String, Object> taskParams = new LinkedHashMap<>();
        taskParams.put("url", context.getCallbackUrl());
        taskParams.put("httpMethod", "GET");
        taskParams.put("httpParams", new ArrayList<>());
        taskParams.put("checkCondition", "STATUS_CODE_DEFAULT");
        taskParams.put("condition", "200");

        // 3. 构建 Task 定义（使用生成的 code）
        Map<String, Object> taskDef = new LinkedHashMap<>();
        taskDef.put("code", taskCode);
        taskDef.put("name", context.getTaskName());
        taskDef.put("taskType", "HTTP");
        taskDef.put("taskParams", taskParams);
        taskDef.put("flag", "YES");
        taskDef.put("description", "");
        taskDef.put("timeoutFlag", "CLOSE");
        taskDef.put("timeoutNotifyStrategy", "");
        taskDef.put("timeout", 0);
        taskDef.put("workerGroup", "default");
        taskDef.put("failRetryTimes", 0);
        taskDef.put("failRetryInterval", 1);
        taskDef.put("environmentCode", -1);
        taskDef.put("delayTime", 0);

        // 4. Task 关系（单节点：preTaskCode=0，postTaskCode=taskCode）
        Map<String, Object> relation = new LinkedHashMap<>();
        relation.put("name", "");
        relation.put("preTaskCode", 0);
        relation.put("preTaskVersion", 0);
        relation.put("postTaskCode", taskCode);
        relation.put("postTaskVersion", 1);
        relation.put("conditionType", "NONE");
        relation.put("conditionParams", new LinkedHashMap<>());

        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(taskDef);
        List<Map<String, Object>> relations = new ArrayList<>();
        relations.add(relation);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("name", context.getTaskName());
        params.put("description", context.getTaskName() + " 调度工作流");
        params.put("taskDefinitionJson", gson.toJson(tasks));
        params.put("taskRelationJson", gson.toJson(relations));

        return params;
    }

    /** 调用 DS gen-task-codes 接口生成有效的 task code */
    private long generateTaskCode() throws IOException {
        String resp = apiClient.genTaskCodes(1);
        log.info("DS gen-task-codes 响应: {}", resp);
        try {
            Map<?, ?> map = gson.fromJson(resp, Map.class);
            if (map != null && map.containsKey("data")) {
                Object data = map.get("data");
                if (data instanceof List && !((List<?>) data).isEmpty()) {
                    Object code = ((List<?>) data).get(0);
                    if (code instanceof Number) {
                        return ((Number) code).longValue();
                    }
                    return Long.parseLong(String.valueOf(code));
                }
            }
        } catch (Exception e) {
            log.warn("解析 DS gen-task-codes 响应失败: {}", resp, e);
        }
        throw new RuntimeException("DS 生成 task code 失败: " + resp);
    }

    /** 构建 Schedule 的 cron JSON（DS 要求的格式） */
    private String buildScheduleCronJson(String cronExpression) {
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("crontab", cronExpression);
        schedule.put("startTime", "");
        schedule.put("endTime", "");
        schedule.put("timezoneId", TimeZone.getDefault().getID());
        return gson.toJson(schedule);
    }

    /** 从 Workflow 详情响应中解析 name */
    private String parseWorkflowName(String json) {
        try {
            Map<?, ?> map = gson.fromJson(json, Map.class);
            if (map != null && map.containsKey("data")) {
                Object data = map.get("data");
                if (data instanceof Map) {
                    Object name = ((Map<?, ?>) data).get("name");
                    return name == null ? null : String.valueOf(name);
                }
            }
        } catch (Exception e) {
            log.warn("解析 DS Workflow 名称失败: {}", json, e);
        }
        return null;
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
