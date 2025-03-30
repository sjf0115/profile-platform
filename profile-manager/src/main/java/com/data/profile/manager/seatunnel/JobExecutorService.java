package com.data.profile.manager.seatunnel;

/**
 * 功能：JobExecutor
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/17 22:38
 */
//@Slf4j
//@Service
public class JobExecutorService {
//    private static Logger LOG = LoggerFactory.getLogger(JobExecutorService.class);
//    private AsyncTaskExecutor taskExecutor;
//
//    public void jobExecute(Long jobDefineId, JobExecParam executeParam) {
////        JobExecutorRes executeResource = jobInstanceService.createExecuteResource(jobDefineId, executeParam);
////        String jobConfig = executeResource.getJobConfig();
////
////        String configFile = writeJobConfigIntoConfFile(jobConfig, jobDefineId);
////        executeJobBySeaTunnel(configFile, executeResource.getJobInstanceId());
//    }
//
//    private String writeJobConfigIntoConfFile(String jobConfig, Long jobDefineId) {
//        String projectRoot = System.getProperty("user.dir");
//        String filePath = projectRoot + File.separator + "profile" + File.separator + jobDefineId + ".conf";
//        try {
//            File file = new File(filePath);
//            if (!file.exists()) {
//                file.getParentFile().mkdirs();
//            }
//
//            FileWriter fileWriter = new FileWriter(file);
//            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//
//            bufferedWriter.write(jobConfig);
//            bufferedWriter.close();
//
//            //log.info("seaTunnel job conf file created and content written successfully.");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return filePath;
//    }
//
//    private void executeJobBySeaTunnel(String filePath, Long jobInstanceId) {
//        try {
//            Common.setDeployMode(DeployMode.CLIENT);
//            JobConfig jobConfig = new JobConfig();
//            jobConfig.setName(jobInstanceId + "_job");
//
//            SeaTunnelClient seaTunnelClient = createSeaTunnelClient();
//            SeaTunnelConfig seaTunnelConfig = new YamlSeaTunnelConfigBuilder().build();
//            ClientJobExecutionEnvironment jobExecutionEnv = seaTunnelClient.createExecutionContext(filePath, jobConfig, seaTunnelConfig);
//
//            ClientJobProxy clientJobProxy = jobExecutionEnv.execute();
//
//            CompletableFuture.runAsync(
//                    () -> {
//                        waitJobFinish(clientJobProxy, jobInstanceId, Long.toString(clientJobProxy.getJobId()), seaTunnelClient);
//                    },
//                    taskExecutor
//            );
//        } catch (Exception e) {
//            // 提交作业实例运行状态
//            //log.error("SeaTunnel job execution submission failed.", e);
//        }
//    }
//
//    private SeaTunnelClient createSeaTunnelClient() {
//        ClientConfig clientConfig = ConfigProvider.locateAndGetClientConfig();
//        return new SeaTunnelClient(clientConfig);
//    }
//
//    private void waitJobFinish(ClientJobProxy clientJobProxy, Long jobInstanceId, String jobEngineId, SeaTunnelClient seaTunnelClient) {
//        ExecutorService executor = Executors.newFixedThreadPool(1);
//        CompletableFuture<JobResult> future = CompletableFuture.supplyAsync(clientJobProxy::waitForJobCompleteV2, executor);
//        JobResult jobResult = new JobResult(JobStatus.FAILED, "");
//        try {
//            jobResult = future.get();
//            executor.shutdown();
//        } catch (InterruptedException e) {
//            jobResult.setError(e.getMessage());
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            jobResult.setError(e.getMessage());
//            throw new RuntimeException(e);
//        } finally {
//            seaTunnelClient.close();
//            //log.info("and jobInstanceService.complete begin");
//        }
//    }
}
