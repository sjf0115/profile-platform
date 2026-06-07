package com.data.engine.plugin.datax.bean;

import com.data.profile.common.domain.engine.ProcessResult;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
@NoArgsConstructor
public class JobTask {
    /** 业务任务 ID */
    private String jobId;
    /** DataX JSON 临时文件绝对路径 */
    private String configPath;
    /** 任务启动时间戳（毫秒） */
    private long startMillis;
    /** 任务超时时间（毫秒），<= 0 表示不超时 */
    private long timeoutMillis;
    /** 任务取消标记 */
    private final AtomicBoolean cancel = new AtomicBoolean(false);
    /** 进程结果（任务结束后填充） */
    private ProcessResult processResult;
    /** DataX 运行期内存日志 */
    private StringBuilder capturedLog = new StringBuilder(8 * 1024);

    public JobTask(String jobId, String configPath) {
        this.jobId = jobId;
        this.configPath = configPath;
        this.startMillis = System.currentTimeMillis();
    }

    public void appendLog(String line) {
        capturedLog.append(line).append('\n');
    }

    public boolean isCancelled() {
        return cancel.get();
    }

    public void markCancel() {
        cancel.set(true);
    }
}
