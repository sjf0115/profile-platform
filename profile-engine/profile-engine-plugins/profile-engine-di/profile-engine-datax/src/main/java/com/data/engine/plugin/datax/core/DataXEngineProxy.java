package com.data.engine.plugin.datax.core;

import com.data.engine.plugin.datax.bean.JobTask;
import com.data.engine.plugin.datax.bean.LogStatistics;
import com.data.engine.plugin.datax.log.AnalysisStatistics;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.enums.engine.ExecutionStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DataX 进程模式执行代理（datax-web 同款）。
 *
 * <p>核心机制：</p>
 * <ol>
 *   <li>把 DataX JSON 落盘到 <code>${java.io.tmpdir}/datax-jobs/&lt;jobId&gt;.json</code></li>
 *   <li>通过 {@link ProcessBuilder} 启动 <code>python ${DATAX_HOME}/bin/datax.py &lt;jobJsonPath&gt;</code></li>
 *   <li>独立 JVM 子进程运行 DataX，stdout/stderr 合流后由后台线程流式写入 {@link JobTask#getCapturedLog()}</li>
 *   <li>{@link Process#waitFor()} 拿 exitCode 判定成败</li>
 *   <li>{@link #cancel(String)} 调用 {@link Process#destroyForcibly()} 实现 OS 级真取消</li>
 * </ol>
 *
 * <p>必备配置：</p>
 * <ul>
 *   <li>系统属性 <code>-Ddatax.home</code> 或环境变量 <code>DATAX_HOME</code> 指向 DataX 安装目录（含 <code>bin/datax.py</code>）</li>
 *   <li>系统属性 <code>-Ddatax.python</code>（可选，默认 <code>python</code>）指定 python 解释器</li>
 * </ul>
 */
@Slf4j
public class DataXEngineProxy {

    private static final String JOB_DIR_NAME = "datax-jobs";
    private static final String DATAX_HOME_ENV = "DATAX_HOME";
    private static final String DATAX_HOME_PROP = "datax.home";
    private static final String PYTHON_CMD_PROP = "datax.python";
    private static final String DATAX_PY_REL = "bin/datax.py";
    private static final long LOG_DRAIN_AWAIT_SECONDS = 5L;
    /** 最大并发任务数，可通过 -Ddatax.max.concurrent 设置，默认 8 */
    private static final int MAX_CONCURRENT_TASKS = Integer.getInteger("datax.max.concurrent", 8);

    /** 运行中子进程表：jobId → Process（用于 cancel 时 destroyForcibly） */
    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

    /** 任务执行线程池（每任务 1 线程，与子进程一一对应） */
    private final ExecutorService taskPool;
    /** 子进程日志读取线程池（每任务 1 线程，读 stdout/stderr 合流） */
    private final ExecutorService logReaderPool;

    private static class DataXEngineProxyHolder {
        private static final DataXEngineProxy INSTANCE = new DataXEngineProxy();
    }

    public static DataXEngineProxy getInstance() {
        return DataXEngineProxyHolder.INSTANCE;
    }

    private DataXEngineProxy() {
        this.taskPool = new ThreadPoolExecutor(
                2, MAX_CONCURRENT_TASKS,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(MAX_CONCURRENT_TASKS * 2),
                new NamedThreadFactory("datax-task"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.logReaderPool = Executors.newCachedThreadPool(new NamedThreadFactory("datax-log-reader"));
    }

    /**
     * 提交 DataX 任务（异步）。
     *
     * @param jobId         业务任务 ID
     * @param jobJson       DataX 标准 JSON 字符串
     * @param timeoutMillis 任务超时时间（毫秒），<= 0 表示不超时
     * @return 异步执行结果
     */
    public CompletableFuture<ProcessResult> submit(@NonNull String jobId, @NonNull String jobJson, long timeoutMillis) {
        Path configPath;
        try {
            configPath = writeJobJson(jobId, jobJson);
        } catch (IOException e) {
            log.error("[DataX] 写入任务 JSON 失败, jobId={}", jobId, e);
            ProcessResult fail = new ProcessResult(ExecutionStatus.FAILURE.getCode());
            fail.setErrorMsg("写入 DataX JSON 失败: " + e.getMessage());
            return CompletableFuture.completedFuture(fail);
        }
        JobTask task = new JobTask(jobId, configPath.toAbsolutePath().toString());
        task.setTimeoutMillis(timeoutMillis);
        return CompletableFuture.supplyAsync(() -> doSubmit(task), taskPool);
    }

    /**
     * 提交 DataX 任务（异步，无超时）。
     *
     * @param jobId   业务任务 ID
     * @param jobJson DataX 标准 JSON 字符串
     * @return 异步执行结果
     */
    public CompletableFuture<ProcessResult> submit(@NonNull String jobId, @NonNull String jobJson) {
        return submit(jobId, jobJson, 0);
    }

    /**
     * 取消任务：对子进程发送 destroyForcibly（OS 级 SIGKILL），实现真取消。
     */
    public void cancel(@NonNull String jobId) {
        Process p = runningProcesses.get(jobId);
        if (p == null) {
            log.warn("[DataX] cancel: jobId={} 不存在或已结束", jobId);
            return;
        }
        try {
            p.destroyForcibly();
            log.info("[DataX] 任务 {} 已发送 destroyForcibly (OS 级 SIGKILL)", jobId);
        } catch (Throwable t) {
            log.error("[DataX] cancel 失败, jobId={}", jobId, t);
        }
    }

    /** 同步执行体：起子进程 → 收日志 → 等结束 */
    private ProcessResult doSubmit(JobTask task) {
        ProcessResult result = new ProcessResult();
        long start = System.currentTimeMillis();
        Process process = null;
        CompletableFuture<Void> logFuture = null;

        try {
            ProcessBuilder pb = buildProcessBuilder(task.getConfigPath());
            pb.redirectErrorStream(true);
            log.info("[DataX] 启动子进程, jobId={}, cmd={}", task.getJobId(), pb.command());

            process = pb.start();
            runningProcesses.put(task.getJobId(), process);
            result.setProcessId(getPid(process));

            // 异步读取子进程合流后的 stdout，每行写入 JobTask.capturedLog 并转发到本地日志框架
            final Process finalProcess = process;
            logFuture = CompletableFuture.runAsync(
                    () -> drainStream(finalProcess.getInputStream(), task), logReaderPool);

            int exitCode;
            if (task.getTimeoutMillis() > 0) {
                boolean finished = process.waitFor(task.getTimeoutMillis(), TimeUnit.MILLISECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    result.setExitStatusCode(ExecutionStatus.FAILURE.getCode());
                    result.setSuccess(false);
                    result.setErrorMsg("DataX 任务超时（" + task.getTimeoutMillis() + "ms），已强制终止");
                    log.error("[DataX] 任务超时, jobId={}, timeout={}ms", task.getJobId(), task.getTimeoutMillis());
                    return result;
                }
                exitCode = process.exitValue();
            } else {
                exitCode = process.waitFor();
            }

            // 等日志读取完成（最多等 5 秒，避免日志被截断）
            try {
                logFuture.get(LOG_DRAIN_AWAIT_SECONDS, TimeUnit.SECONDS);
            } catch (Throwable ignored) {
                // 日志读取超时不影响结果
            }

            if (exitCode == 0) {
                result.setExitStatusCode(ExecutionStatus.SUCCESS.getCode());
                result.setSuccess(true);
                log.info("[DataX] 任务成功, jobId={}, exitCode=0", task.getJobId());
            } else {
                result.setExitStatusCode(ExecutionStatus.FAILURE.getCode());
                result.setSuccess(false);
                result.setErrorMsg("DataX 子进程 exitCode=" + exitCode);
                log.error("[DataX] 任务失败, jobId={}, exitCode={}", task.getJobId(), exitCode);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            result.setExitStatusCode(ExecutionStatus.FAILURE.getCode());
            result.setSuccess(false);
            result.setErrorMsg("DataX 任务等待过程被中断: " + ie.getMessage());
            log.error("[DataX] 任务中断, jobId={}", task.getJobId(), ie);
        } catch (Throwable t) {
            result.setExitStatusCode(ExecutionStatus.FAILURE.getCode());
            result.setSuccess(false);
            result.setErrorMsg(t.getMessage());
            log.error("[DataX] 任务执行异常, jobId={}", task.getJobId(), t);
        } finally {
            runningProcesses.remove(task.getJobId());
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            result.setDuration(System.currentTimeMillis() - start);

            // 解析 DataX 标准日志统计（7 项）
            LogStatistics stats = AnalysisStatistics.analysis(task.getCapturedLog().toString());
            fillFromStatistics(result, stats);
            task.setProcessResult(result);
        }
        return result;
    }

    /** 后台线程读取子进程 stdout/stderr 合流，每行写入 JobTask 并转发到 SLF4J */
    private void drainStream(InputStream is, JobTask task) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                task.appendLog(line);
                log.info("[DataX][{}] {}", task.getJobId(), line);
            }
        } catch (IOException e) {
            log.warn("[DataX] 读取子进程日志异常, jobId={}", task.getJobId(), e);
        }
    }

    /** 构造 ProcessBuilder：python ${DATAX_HOME}/bin/datax.py {jobJsonPath} */
    private ProcessBuilder buildProcessBuilder(String jobConfigPath) {
        String dataxHome = resolveDataxHome();
        Path pyPath = Paths.get(dataxHome, DATAX_PY_REL);
        if (!Files.exists(pyPath)) {
            throw new IllegalStateException("DataX 启动脚本不存在: " + pyPath
                    + "，请检查 DATAX_HOME 配置是否指向有效的 DataX 安装目录");
        }
        String python = System.getProperty(PYTHON_CMD_PROP, "python");

        List<String> cmd = new ArrayList<>(3);
        cmd.add(python);
        cmd.add(pyPath.toAbsolutePath().toString());
        cmd.add(jobConfigPath);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().put(DATAX_HOME_ENV, dataxHome);
        return pb;
    }

    /** 解析 DataX 安装目录：优先系统属性 -Ddatax.home，其次环境变量 DATAX_HOME */
    private String resolveDataxHome() {
        String home = System.getProperty(DATAX_HOME_PROP);
        if (StringUtils.isBlank(home)) {
            home = System.getenv(DATAX_HOME_ENV);
        }
        if (StringUtils.isBlank(home)) {
            throw new IllegalStateException("未配置 DataX 安装目录："
                    + "请设置环境变量 DATAX_HOME 或 JVM 系统属性 -Ddatax.home=/path/to/datax");
        }
        return home;
    }

    /** 反射拿 PID（Java 1.8 没有 Process.pid()，UNIX 平台读 UNIXProcess.pid 字段） */
    private Integer getPid(Process p) {
        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            return f.getInt(p);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    /** 写入 JSON 到临时目录 */
    private Path writeJobJson(String jobId, String jobJson) throws IOException {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"), JOB_DIR_NAME);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path file = dir.resolve(jobId + ".json");
        Files.write(file, jobJson.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /** 把日志统计回填到 ProcessResult */
    private void fillFromStatistics(ProcessResult result, LogStatistics stats) {
        if (stats == null) {
            return;
        }
        String readRecords = stats.getTaskRecordReaderNum();
        if (StringUtils.isNotBlank(readRecords)) {
            try {
                result.setRecordCount(Long.parseLong(readRecords.trim()));
            } catch (NumberFormatException ignored) {
                // 日志格式异常忽略
            }
        }
    }

    /** 命名线程工厂 */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger seq = new AtomicInteger(0);

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
