package com.data.engine.plugin.datax.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LogStatistics {
    /** 任务启动时刻 */
    private String taskStartTime;
    /** 任务结束时刻 */
    private String taskEndTime;
    /** 任务总计耗时（如 0.18s） */
    private String taskTotalTime;
    /** 任务平均流量（如 1.20MB/s） */
    private String taskAverageFlow;
    /** 记录写入速度（如 100rec/s） */
    private String taskRecordWritingSpeed;
    /** 读出记录总数 */
    private String taskRecordReaderNum;
    /** 读写失败总数 */
    private String taskRecordWriteFailNum;
}
