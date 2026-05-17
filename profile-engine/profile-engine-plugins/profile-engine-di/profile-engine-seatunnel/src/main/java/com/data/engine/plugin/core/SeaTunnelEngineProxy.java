package com.data.engine.plugin.core;

import com.hazelcast.client.config.ClientConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.seatunnel.engine.client.SeaTunnelClient;
import org.apache.seatunnel.engine.client.job.ClientJobExecutionEnvironment;
import org.apache.seatunnel.engine.client.job.ClientJobProxy;
import org.apache.seatunnel.engine.common.config.ConfigProvider;
import org.apache.seatunnel.engine.common.config.JobConfig;
import org.apache.seatunnel.engine.common.config.SeaTunnelConfig;
import org.apache.seatunnel.engine.common.config.YamlSeaTunnelConfigBuilder;
import org.apache.seatunnel.engine.core.job.JobDAGInfo;
import org.apache.seatunnel.engine.core.job.JobResult;
import org.apache.seatunnel.engine.core.job.JobStatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SeaTunnelEngineProxy {

    private ClientConfig clientConfig = null;

    private static class SeaTunnelEngineProxyHolder {
        private static final SeaTunnelEngineProxy INSTANCE = new SeaTunnelEngineProxy();
    }

    public static SeaTunnelEngineProxy getInstance() {
        return SeaTunnelEngineProxyHolder.INSTANCE;
    }

    private SeaTunnelEngineProxy() {
        clientConfig = ConfigProvider.locateAndGetClientConfig();
    }

    /**
     * 执行作业
     * @param filePath 作业配置路径
     * @param jobId 作业ID
     */
    public void executeJob(@NonNull String filePath, @NonNull String jobId) {
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobId + "_job");
        SeaTunnelConfig seaTunnelConfig = new YamlSeaTunnelConfigBuilder().build();
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)) {
            ClientJobExecutionEnvironment environment = seaTunnelClient.createExecutionContext(filePath, jobConfig, seaTunnelConfig);
            ClientJobProxy clientJobProxy = environment.execute();
            long jobInstanceId = clientJobProxy.getJobId();
            log.info("提交 SeaTunnel 任务, 任务ID: {}, 任务实例ID: {}", jobId, jobInstanceId);
            ExecutorService executor = Executors.newFixedThreadPool(1);
            CompletableFuture<JobResult> future = CompletableFuture.supplyAsync(clientJobProxy::waitForJobCompleteV2, executor);
            JobResult jobResult = future.get();
            log.info(".....................: {}", jobResult.getStatus());
            executor.shutdown();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 暂停作业
     * @param jobId 作业ID
     */
    public void pauseJob(@NonNull String jobId) {
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)) {
            seaTunnelClient.getJobClient().savePointJob(Long.valueOf(jobId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 恢复作业
     * @param filePath 作业配置路径
     * @param jobInstanceId
     * @param jobId 作业ID
     */
    public void restoreJob(@NonNull String filePath, @NonNull Long jobInstanceId, @NonNull Long jobId) {
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobInstanceId + "_job");
        SeaTunnelConfig seaTunnelConfig = new YamlSeaTunnelConfigBuilder().build();
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)) {
            ClientJobExecutionEnvironment environment = seaTunnelClient.restoreExecutionContext(filePath, jobConfig, seaTunnelConfig, jobId);
            environment.execute();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 作业详情
     * @param jobId 作业ID
     */
    public JobDAGInfo getJobInfo(@NonNull String jobId) {
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)){
            return seaTunnelClient.getJobInfo(Long.valueOf(jobId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试引擎连通性
     * 通过连接 SeaTunnel 集群并获取健康指标来验证连通性
     *
     * @return 连通性检测结果，包含集群健康信息
     */
    public Map<String, String> testConnection() {
        Map<String, String> healthMetrics = new java.util.LinkedHashMap<>();
        long startTime = System.currentTimeMillis();
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)){
            // 获取集群健康指标验证连通性
            healthMetrics = seaTunnelClient.getClusterHealthMetrics();
            long duration = System.currentTimeMillis() - startTime;
            healthMetrics.put("connected", "true");
            healthMetrics.put("duration", duration + "ms");
            log.info("SeaTunnel 引擎连通性测试成功, 耗时: {}ms", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            healthMetrics.put("connected", "false");
            healthMetrics.put("duration", duration + "ms");
            healthMetrics.put("error", e.getMessage());
            log.error("SeaTunnel 引擎连通性测试失败, 耗时: {}ms", duration, e);
        }
        return healthMetrics;
    }

    //------------------------------------------------------------------------------------------------------------------

    public String getMetricsContent(@NonNull String jobEngineId) {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobMetrics(Long.valueOf(jobEngineId));
        } finally {
            seaTunnelClient.close();
        }
    }

    public String getJobPipelineStatusStr(@NonNull String jobEngineId) {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobDetailStatus(Long.valueOf(jobEngineId));
        } finally {
            seaTunnelClient.close();
        }
    }

    public JobStatus getJobStatus(@NonNull String jobEngineId) {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return JobStatus.valueOf(seaTunnelClient.getJobStatus(Long.valueOf(jobEngineId)));
        } catch (Exception e) {
            log.warn("Can not get job from engine.", e);
            return null;
        } finally {
            seaTunnelClient.close();
        }
    }

    public Map<String, String> getClusterHealthMetrics() {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getClusterHealthMetrics();
        } finally {
            seaTunnelClient.close();
        }
    }

    public String getAllRunningJobMetricsContent() {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobClient().getRunningJobMetrics();
        } finally {
            seaTunnelClient.close();
        }
    }
}
