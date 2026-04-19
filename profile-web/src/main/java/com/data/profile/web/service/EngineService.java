package com.data.profile.web.service;

import com.data.engine.api.EngineExecutor;
import com.data.engine.api.EngineFactory;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.SeaTunnelEngineFactory;
import com.data.engine.plugin.bean.JobTask;
import com.data.engine.plugin.executor.SeaTunnelEngineExecutor;
import com.data.engine.plugin.utils.SeaTunnelConfigUtil;
import com.data.profile.common.domain.Constant;
import com.data.profile.common.domain.connector.request.TestConnectionRequestParam;
import com.data.profile.common.utils.FileUtil;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Task;
import com.data.spi.PluginLoader;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 功能：Engine 测试
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 13:00
 */
@Slf4j
@Service
public class EngineService {

    @Resource
    private DataSourceService dataSourceService;

    /**
     * 测试引擎连通性
     * @param param 参数
     */
    public void testConnect(TestConnectionRequestParam param) {
        EngineFactory engineFactory = PluginLoader.getPluginLoader(EngineFactory.class).getOrCreatePlugin(Constant.ENGINE_SEATUNNEL);
        try {
            engineFactory.getExecutor().execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void executeDiTask(String jobId) {
        // 1. 生成作业配置
        String config = "";

        // 2. 生成配置文件
        String projectRoot = System.getProperty("user.dir");
        String filePath = projectRoot + File.separator + "config" + File.separator + jobId + ".conf";
        FileUtil.writeFile(config, filePath);

        filePath = "/opt/workspace/seatunnel/profile/mysql_to_mysql.conf";

        // 3. 提交集群执行
        EngineFactory engineFactory = PluginLoader.getPluginLoader(EngineFactory.class).getOrCreatePlugin(Constant.ENGINE_SEATUNNEL);
        try {
            // 执行器
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

    public void executeGroupTask() {

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
