package com.data.engine.plugin.schedule.dolphinscheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * DolphinSchedulerApiClient 集成测试。
 *
 * <p>测试前提：DolphinScheduler 服务运行在 localhost:12345。</p>
 * <p>运行方式：mvn test -pl profile-engine/profile-engine-plugins/profile-engine-schedule/profile-engine-dolphinscheduler</p>
 *
 * <p>注意：带 @Ignore 的测试方法需要真实 DS 环境，手动去掉注解后运行。</p>
 */
public class DolphinSchedulerApiClientTest {

    private static final Gson gson = new GsonBuilder().create();

    private DolphinSchedulerApiClient apiClient;

    // === 配置项（根据实际环境修改） ===
    private static final String API_URL = "http://localhost:12345/dolphinscheduler";
    private static final String TOKEN = "your-ds-token-here";  // 替换为真实 token
    private static final String PROJECT_CODE = "22012166559808"; // 替换为真实 projectCode
    private static final String TENANT_CODE = "default";

    @Before
    public void setUp() {
        apiClient = new DolphinSchedulerApiClient();
        Map<String, Object> config = new HashMap<>();
        config.put("apiUrl", API_URL);
        config.put("token", TOKEN);
        config.put("projectCode", PROJECT_CODE);
        config.put("tenantCode", TENANT_CODE);
        apiClient.init(config);
    }

    // -----------------------------------------------------------------
    // 1. 连通性测试
    // -----------------------------------------------------------------

    @Test
    @Ignore("需要真实 DS 服务，手动启用")
    public void testConnection_shouldReturnHealthInfo() throws IOException {
        String resp = apiClient.testConnection();
        System.out.println("testConnection 响应: " + resp);
        assertNotNull("响应不应为空", resp);
        assertFalse("响应不应为空字符串", resp.isEmpty());
    }

    // -----------------------------------------------------------------
    // 2. Task Code 生成测试
    // -----------------------------------------------------------------

    @Test
    @Ignore("需要真实 DS 服务，手动启用")
    public void genTaskCodes_shouldReturnValidCode() throws IOException {
        String resp = apiClient.genTaskCodes(1);
        System.out.println("genTaskCodes 响应: " + resp);

        Map<?, ?> map = gson.fromJson(resp, Map.class);
        assertNotNull("响应 JSON 不应为空", map);

        // DS 成功响应: {"code":0,"msg":"success","data":[123456789]}
        Object code = map.get("code");
        assertEquals("DS 响应 code 应为 0", 0.0, ((Number) code).doubleValue(), 0);

        Object data = map.get("data");
        assertTrue("data 应为列表", data instanceof List);
        assertFalse("data 不应为空列表", ((List<?>) data).isEmpty());

        Object taskCode = ((List<?>) data).get(0);
        assertTrue("task code 应为数字", taskCode instanceof Number);
        System.out.println("生成的 task code: " + taskCode);
    }

    @Test
    @Ignore("需要真实 DS 服务，手动启用")
    public void genTaskCodes_multiple_shouldReturnList() throws IOException {
        String resp = apiClient.genTaskCodes(5);
        Map<?, ?> map = gson.fromJson(resp, Map.class);
        List<?> data = (List<?>) map.get("data");
        assertEquals("应返回 5 个 task code", 5, data.size());
        System.out.println("生成的 task codes: " + data);
    }

    // -----------------------------------------------------------------
    // 3. 工作流定义 CRUD 测试（端到端流程）
    // -----------------------------------------------------------------

    /**
     * 端到端测试：创建 → 查询 → 上线 → 下线 → 删除
     * 注意：此测试会真实创建 DS 工作流，测试结束后会自动清理
     */
    @Test
    @Ignore("需要真实 DS 服务，手动启用")
    public void workflowLifecycle_shouldSucceed() throws IOException {
        String workflowCode = null;
        String workflowName = "test-workflow-" + System.currentTimeMillis();

        try {
            // === Step 1: 生成 Task Code ===
            String genResp = apiClient.genTaskCodes(1);
            Map<?, ?> genMap = gson.fromJson(genResp, Map.class);
            long taskCode = ((Number) ((List<?>) genMap.get("data")).get(0)).longValue();
            System.out.println("[1/6] 生成 task code: " + taskCode);

            // === Step 2: 创建工作流 ===
            Map<String, Object> taskDef = new LinkedHashMap<>();
            taskDef.put("code", taskCode);
            taskDef.put("name", "test-http-task");
            taskDef.put("taskType", "HTTP");
            taskDef.put("taskParams", buildHttpTaskParams());
            taskDef.put("flag", "YES");
            taskDef.put("timeoutFlag", "CLOSE");
            taskDef.put("timeout", 0);
            taskDef.put("workerGroup", "default");
            taskDef.put("failRetryTimes", 0);
            taskDef.put("failRetryInterval", 1);
            taskDef.put("environmentCode", -1);
            taskDef.put("delayTime", 0);

            Map<String, Object> relation = new LinkedHashMap<>();
            relation.put("name", "");
            relation.put("preTaskCode", 0);
            relation.put("preTaskVersion", 0);
            relation.put("postTaskCode", taskCode);
            relation.put("postTaskVersion", 1);
            relation.put("conditionType", "NONE");
            relation.put("conditionParams", new LinkedHashMap<>());

            Map<String, String> createParams = new LinkedHashMap<>();
            createParams.put("name", workflowName);
            createParams.put("description", "测试工作流");
            createParams.put("taskDefinitionJson", gson.toJson(Collections.singletonList(taskDef)));
            createParams.put("taskRelationJson", gson.toJson(Collections.singletonList(relation)));

            String createResp = apiClient.createWorkflow(createParams);
            System.out.println("[2/6] 创建工作流响应: " + createResp);

            Map<?, ?> createMap = gson.fromJson(createResp, Map.class);
            assertEquals("创建工作流应返回 code=0", 0.0, ((Number) createMap.get("code")).doubleValue(), 0);

            Object data = createMap.get("data");
            if (data instanceof Map) {
                workflowCode = String.valueOf(((Map<?, ?>) data).get("code"));
            } else {
                workflowCode = String.valueOf(data);
            }
            assertNotNull("应获取到 workflowCode", workflowCode);
            System.out.println("    workflowCode: " + workflowCode);

            // === Step 3: 查询工作流 ===
            String queryResp = apiClient.getWorkflowStatus(workflowCode);
            System.out.println("[3/6] 查询工作流响应: " + queryResp.substring(0, Math.min(200, queryResp.length())) + "...");
            assertNotNull(queryResp);
            assertTrue("查询响应应包含工作流名称", queryResp.contains(workflowName));

            // === Step 4: 上线工作流 ===
            String onlineResp = apiClient.onlineWorkflow(workflowCode, workflowName);
            System.out.println("[4/6] 上线工作流响应: " + onlineResp);

            // === Step 5: 下线工作流 ===
            String offlineResp = apiClient.offlineWorkflow(workflowCode, workflowName);
            System.out.println("[5/6] 下线工作流响应: " + offlineResp);

            // === Step 6: 删除工作流 ===
            String deleteResp = apiClient.deleteWorkflow(workflowCode);
            System.out.println("[6/6] 删除工作流响应: " + deleteResp);

        } finally {
            // 确保清理：如果测试中途失败，尝试删除已创建的工作流
            if (workflowCode != null) {
                try {
                    apiClient.offlineWorkflow(workflowCode, workflowName);
                } catch (Exception ignored) {}
                try {
                    apiClient.deleteWorkflow(workflowCode);
                } catch (Exception ignored) {}
            }
        }
    }

    // -----------------------------------------------------------------
    // 4. 调度 Schedule 测试
    // -----------------------------------------------------------------

    @Test
    @Ignore("需要真实 DS 服务 + 已存在的工作流，手动启用")
    public void scheduleLifecycle_shouldSucceed() throws IOException {
        // 前置：创建测试工作流（复用 workflowLifecycle 逻辑）
        String workflowName = "test-schedule-" + System.currentTimeMillis();
        String workflowCode = createTestWorkflow(workflowName);
        String scheduleId = null;

        try {
            // === Step 1: 创建调度 ===
            Map<String, Object> cronSchedule = new LinkedHashMap<>();
            cronSchedule.put("crontab", "0 0 1 * * ? *");
            cronSchedule.put("startTime", "");
            cronSchedule.put("endTime", "");
            cronSchedule.put("timezoneId", TimeZone.getDefault().getID());

            String scheduleResp = apiClient.createSchedule(workflowCode, gson.toJson(cronSchedule));
            System.out.println("[1/3] 创建调度响应: " + scheduleResp);

            Map<?, ?> scheduleMap = gson.fromJson(scheduleResp, Map.class);
            if (scheduleMap.get("data") instanceof Map) {
                scheduleId = String.valueOf(((Map<?, ?>) scheduleMap.get("data")).get("id"));
            }
            System.out.println("    scheduleId: " + scheduleId);

            if (scheduleId != null) {
                // === Step 2: 上线调度 ===
                String onlineResp = apiClient.onlineSchedule(scheduleId);
                System.out.println("[2/3] 上线调度响应: " + onlineResp);

                // === Step 3: 下线调度 ===
                String offlineResp = apiClient.offlineSchedule(scheduleId);
                System.out.println("[3/3] 下线调度响应: " + offlineResp);
            }
        } finally {
            // 清理
            cleanupWorkflow(workflowCode, workflowName);
        }
    }

    // -----------------------------------------------------------------
    // 5. 手动触发测试
    // -----------------------------------------------------------------

    @Test
    @Ignore("需要真实 DS 服务 + 已上线的工作流，手动启用")
    public void triggerWorkflow_shouldStartProcessInstance() throws IOException {
        String workflowName = "test-trigger-" + System.currentTimeMillis();
        String workflowCode = createTestWorkflow(workflowName);

        try {
            // 先上线工作流
            apiClient.onlineWorkflow(workflowCode, workflowName);

            // 触发执行
            String triggerResp = apiClient.triggerWorkflow(workflowCode);
            System.out.println("触发工作流响应: " + triggerResp);
            assertNotNull(triggerResp);

        } finally {
            cleanupWorkflow(workflowCode, workflowName);
        }
    }

    // -----------------------------------------------------------------
    // 6. 错误场景测试
    // -----------------------------------------------------------------

    @Test
    @Ignore("需要真实 DS 服务，手动启用")
    public void getWorkflowStatus_invalidCode_shouldReturnError() throws IOException {
        String resp = apiClient.getWorkflowStatus("999999999999");
        System.out.println("查询不存在的 workflow 响应: " + resp);
        Map<?, ?> map = gson.fromJson(resp, Map.class);
        // DS 对于不存在的 code 通常返回非 0 的 code
        Object code = map.get("code");
        assertNotEquals("查询不存在的 workflow 应返回错误 code", 0.0, ((Number) code).doubleValue(), 0);
    }

    @Test
    @Ignore("需要真实 DS 服务，手动启用")
    public void deleteWorkflow_invalidCode_shouldReturnError() throws IOException {
        String resp = apiClient.deleteWorkflow("999999999999");
        System.out.println("删除不存在的 workflow 响应: " + resp);
        assertNotNull(resp);
    }

    // -----------------------------------------------------------------
    // 辅助方法
    // -----------------------------------------------------------------

    private Map<String, Object> buildHttpTaskParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("url", "http://localhost:8080/api/health");
        params.put("httpMethod", "GET");
        params.put("httpParams", new ArrayList<>());
        params.put("checkCondition", "STATUS_CODE_DEFAULT");
        params.put("condition", "200");
        return params;
    }

    private String createTestWorkflow(String name) throws IOException {
        String genResp = apiClient.genTaskCodes(1);
        Map<?, ?> genMap = gson.fromJson(genResp, Map.class);
        long taskCode = ((Number) ((List<?>) genMap.get("data")).get(0)).longValue();

        Map<String, Object> taskDef = new LinkedHashMap<>();
        taskDef.put("code", taskCode);
        taskDef.put("name", name + "-task");
        taskDef.put("taskType", "HTTP");
        taskDef.put("taskParams", buildHttpTaskParams());
        taskDef.put("flag", "YES");
        taskDef.put("timeoutFlag", "CLOSE");
        taskDef.put("timeout", 0);
        taskDef.put("workerGroup", "default");
        taskDef.put("failRetryTimes", 0);
        taskDef.put("failRetryInterval", 1);
        taskDef.put("environmentCode", -1);
        taskDef.put("delayTime", 0);

        Map<String, Object> relation = new LinkedHashMap<>();
        relation.put("name", "");
        relation.put("preTaskCode", 0);
        relation.put("preTaskVersion", 0);
        relation.put("postTaskCode", taskCode);
        relation.put("postTaskVersion", 1);
        relation.put("conditionType", "NONE");
        relation.put("conditionParams", new LinkedHashMap<>());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("name", name);
        params.put("description", "测试用工作流");
        params.put("taskDefinitionJson", gson.toJson(Collections.singletonList(taskDef)));
        params.put("taskRelationJson", gson.toJson(Collections.singletonList(relation)));

        String resp = apiClient.createWorkflow(params);
        Map<?, ?> respMap = gson.fromJson(resp, Map.class);
        Object data = respMap.get("data");
        if (data instanceof Map) {
            return String.valueOf(((Map<?, ?>) data).get("code"));
        }
        return String.valueOf(data);
    }

    private void cleanupWorkflow(String workflowCode, String workflowName) {
        try {
            apiClient.offlineWorkflow(workflowCode, workflowName);
        } catch (Exception ignored) {}
        try {
            apiClient.deleteWorkflow(workflowCode);
        } catch (Exception ignored) {}
    }
}
package com.data.engine.plugin.schedule.dolphinscheduler;

public class DolphinSchedulerApiClientTest {
    
}
