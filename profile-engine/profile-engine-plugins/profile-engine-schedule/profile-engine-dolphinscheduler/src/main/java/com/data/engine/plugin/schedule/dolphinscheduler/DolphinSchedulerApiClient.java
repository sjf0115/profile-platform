package com.data.engine.plugin.schedule.dolphinscheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * DolphinScheduler OpenAPI 封装。
 *
 * <p>基于 HttpClient 调用 DolphinScheduler RESTful API。
 * 配置项从 Engine 表的 config 字段读取：apiUrl、token、projectCode。</p>
 */
@Slf4j
public class DolphinSchedulerApiClient {

    private static final Gson gson = new GsonBuilder().create();

    private String apiUrl;
    private String token;
    private String projectCode;

    /** 初始化 API 客户端配置 */
    public void init(Map<String, Object> config) {
        this.apiUrl = getString(config, "apiUrl");
        this.token = getString(config, "token");
        this.projectCode = getString(config, "projectCode");
        log.info("DolphinSchedulerApiClient 初始化: apiUrl={}, projectCode={}", apiUrl, projectCode);
    }

    /** 创建工作流定义 */
    public String createWorkflow(String workflowJson) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/workflows";
        return doPost(url, workflowJson);
    }

    /** 更新工作流定义 */
    public String updateWorkflow(String workflowCode, String workflowJson) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/workflows/" + workflowCode;
        return doPut(url, workflowJson);
    }

    /** 删除工作流定义 */
    public String deleteWorkflow(String workflowCode) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/workflows/" + workflowCode;
        return doDelete(url);
    }

    /** 上线工作流 */
    public String onlineWorkflow(String workflowCode) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/workflows/" + workflowCode + "/online";
        return doPost(url, "");
    }

    /** 下线工作流 */
    public String offlineWorkflow(String workflowCode) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/workflows/" + workflowCode + "/offline";
        return doPost(url, "");
    }

    /** 创建定时调度 */
    public String createSchedule(String workflowCode, String scheduleJson) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/schedules";
        return doPost(url, scheduleJson);
    }

    /** 更新定时调度 */
    public String updateSchedule(String scheduleId, String scheduleJson) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/schedules/" + scheduleId;
        return doPut(url, scheduleJson);
    }

    /** 上线调度 */
    public String onlineSchedule(String scheduleId) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/schedules/" + scheduleId + "/online";
        return doPost(url, "");
    }

    /** 下线调度 */
    public String offlineSchedule(String scheduleId) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/schedules/" + scheduleId + "/offline";
        return doPost(url, "");
    }

    /** 手动触发工作流执行 */
    public String triggerWorkflow(String workflowCode) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/executors/start-process-instance";
        String body = gson.toJson(new java.util.HashMap<String, Object>() {{
            put("workflowCode", workflowCode);
        }});
        return doPost(url, body);
    }

    /** 查询工作流状态 */
    public String getWorkflowStatus(String workflowCode) throws IOException {
        String url = apiUrl + "/projects/" + projectCode + "/workflows/" + workflowCode;
        return doGet(url);
    }

    /**
     * 测试连通性。
     * 调用 DolphinScheduler 的 /actuator/health 端点，返回服务健康信息。
     * 如果该端点不可用，则尝试访问登录页面以验证服务可达。
     *
     * @return 健康信息 JSON 字符串
     * @throws IOException 连接失败时抛出
     */
    public String testConnection() throws IOException {
        // 尝试访问 DolphinScheduler actuator 健康检查端点
        // DS 地址格式如： http://ds-host:12345/dolphinscheduler，健康端点在其上走 /actuator/health
        String baseUrl = apiUrl.replaceAll("/dolphinscheduler.*", "");
        String healthUrl = baseUrl + "/dolphinscheduler/actuator/health";
        return doGetNoAuth(healthUrl);
    }

    // -----------------------------------------------------------------
    // HTTP 方法
    // -----------------------------------------------------------------

    private String doGet(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("token", token);
            request.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String doGetNoAuth(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String doPost(String url, String body) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.setHeader("token", token);
            request.setHeader("Content-Type", "application/json");
            if (body != null && !body.isEmpty()) {
                request.setEntity(new StringEntity(body, "UTF-8"));
            }
            HttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String doPut(String url, String body) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(url);
            request.setHeader("token", token);
            request.setHeader("Content-Type", "application/json");
            if (body != null && !body.isEmpty()) {
                request.setEntity(new StringEntity(body, "UTF-8"));
            }
            HttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String doDelete(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(url);
            request.setHeader("token", token);
            request.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
