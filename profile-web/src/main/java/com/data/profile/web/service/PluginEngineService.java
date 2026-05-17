package com.data.profile.web.service;

import com.data.engine.api.EngineExecutor;
import com.data.engine.api.EngineFactory;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.bean.JobTask;
import com.data.engine.plugin.core.SeaTunnelEngineProxy;
import com.data.engine.plugin.utils.SeaTunnelConfigUtil;
import com.data.profile.common.domain.Constant;
import com.data.profile.common.domain.connector.request.TestConnectionRequestParam;
import com.data.profile.common.utils.FileUtil;
import com.data.profile.web.model.DataSource;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 功能：Engine 服务
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 13:00
 */
@Slf4j
@Service
public class PluginEngineService {

    @Resource
    private DataSourceService dataSourceService;

    /**
     * 测试引擎连通性
     * 连接 SeaTunnel 引擎集群，获取集群健康指标，验证连通性
     *
     * @param param 请求参数（预留，当前未使用）
     * @return 连通性检测结果
     */
    public Map<String, String> testConnect(TestConnectionRequestParam param) {
        log.info("开始测试 SeaTunnel 引擎连通性");
        return SeaTunnelEngineProxy.getInstance().testConnection();
    }

    /**
     * 提交作业执行
     * @param jobId
     */
    public void executeDiTask(String jobId) {
        /*// 1. 生成作业配置
        String config = "";

        // 2. 生成配置文件
        String projectRoot = System.getProperty("user.dir");
        String filePath = projectRoot + File.separator + "config" + File.separator + jobId + ".conf";
        FileUtil.writeFile(config, filePath);*/

        String filePath = "/opt/workspace/apache-seatunnel-web-1.0.2-bin/profile/21343715957248.conf";

        // 3. 提交集群执行
        EngineFactory engineFactory = PluginLoader.getPluginLoader(EngineFactory.class).getOrCreatePlugin(Constant.ENGINE_SEATUNNEL);
        try {
            EngineExecutor executor = engineFactory.getExecutor();
            ExecutorRequest request = ExecutorRequest.builder()
                    .configPath(filePath)
                    .jobId("1222")
                    .build();
            executor.init(request, log, null);
            // 执行任务
            executor.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交 SeaTunnel 任务
     * @return 任务ID
     */
    public String submitJob(String jobConfig) {
        // 1. 生成任务ID
        String jobId = "JOB_" + System.currentTimeMillis();

        // 2. 生成配置文件
        String projectRoot = System.getProperty("user.dir");
        String filePath = projectRoot + File.separator + "config" + File.separator + jobId + ".conf";
        FileUtil.writeFile(jobConfig, filePath);

        // 3. 提交集群执行
        EngineFactory engineFactory = PluginLoader.getPluginLoader(EngineFactory.class).getOrCreatePlugin(Constant.ENGINE_SEATUNNEL);
        try {
            // 执行器
            EngineExecutor executor = engineFactory.getExecutor();
            ExecutorRequest request = ExecutorRequest.builder()
                    .configPath(filePath)
                    .jobId(jobId)
                    .build();
            executor.init(request, log, null);

            // 异步执行任务
            new Thread(() -> {
                try {
                    executor.execute();
                    log.info("Job executed successfully: {}", jobId);
                } catch (Exception e) {
                    log.error("Job execution failed: {}", jobId, e);
                }
            }).start();

            return jobId;

        } catch (Exception e) {
            throw new RuntimeException("提交任务失败: " + e.getMessage(), e);
        }
    }

    public void test() {
        JobTask task = JobTask.builder()
                .type("source")
                .connectorType("jdbc")
                .name("test-source-job")
                .config("{\"schema_save_mode\":\"CREATE_SCHEMA_WHEN_NOT_EXIST\",\"data_save_mode\":\"APPEND_DATA\",\"create_index\":\"true\",\"connection_check_timeout_sec\":\"30\",\"batch_size\":\"1000\",\"is_exactly_once\":\"false\",\"xa_data_source_class_name\":\"\",\"max_commit_attempts\":\"3\",\"transaction_timeout_sec\":\"-1\",\"max_retries\":\"0\",\"auto_commit\":\"true\",\"support_upsert_by_query_primary_key_exist\":\"false\",\"primary_keys\":\"\",\"compatible_mode\":\"\",\"multi_table_sink_replica\":\"1\"}")
                .selectTableFields("{\"tableFields\":[\"id\",\"name\",\"age\",\"email\"],\"all\":true}")
                .dataSourceOption("")
                .outputSchema("[{\"fields\":[{\"type\":\"BIGINT\",\"name\":\"id\",\"comment\":\"主键ID\",\"primaryKey\":true,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"BIGINT\"},{\"type\":\"VARCHAR\",\"name\":\"name\",\"comment\":\"姓名\",\"primaryKey\":false,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"STRING\"},{\"type\":\"INT\",\"name\":\"age\",\"comment\":\"年龄\",\"primaryKey\":false,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"INT\"},{\"type\":\"VARCHAR\",\"name\":\"email\",\"comment\":\"邮箱\",\"primaryKey\":false,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"STRING\"}],\"tableName\":\"tb_user\",\"database\":\"test\"}]")
                .dataSourceId(11212L)
                .build();

        DataSource dataSource = dataSourceService.getDetail("");
        String config = dataSource.getConfig();


        try {
            String result = SeaTunnelConfigUtil.generateJobConfig(task);
            log.info("---------------------------{}", result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
