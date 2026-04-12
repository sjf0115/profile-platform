package com.data.profile.web.service;

import com.data.engine.api.EngineExecutor;
import com.data.engine.api.EngineFactory;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.SeaTunnelEngineFactory;
import com.data.engine.plugin.executor.SeaTunnelEngineExecutor;
import com.data.profile.common.domain.Constant;
import com.data.profile.common.domain.connector.request.TestConnectionRequestParam;
import com.data.profile.common.utils.FileUtil;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

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
}
