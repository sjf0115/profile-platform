package com.data.engine.plugin.datax.log;

import com.data.engine.plugin.datax.bean.LogStatistics;

/**
 * 解析 DataX 子进程标准输出中的任务统计摘要。
 *
 * DataX 任务结束后会在日志末尾打印如下固定格式的 7 行统计：
 * <pre>
 * 任务启动时刻                    : 2026-06-13 13:37:10
 * 任务结束时刻                    : 2026-06-13 13:37:12
 * 任务总计耗时                    :                  2s
 * 任务平均流量                    :             1.20MB/s
 * 记录写入速度                    :              100rec/s
 * 读出记录总数                    :                200
 * 读写失败总数                    :                  0
 * </pre>
 */
public final class AnalysisStatistics {

    private AnalysisStatistics() {}

    /**
     * 从 DataX 任务日志中解析统计摘要。
     *
     * @param log 完整日志字符串
     * @return 解析结果；日志为空或格式异常时返回空对象（不抛异常）
     */
    public static LogStatistics analysis(String log) {
        LogStatistics stats = new LogStatistics();
        if (log == null || log.isEmpty()) {
            return stats;
        }
        for (String line : log.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.contains(":")) {
                int idx = trimmed.indexOf(':');
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                switch (key) {
                    case "任务启动时刻":
                        stats.setTaskStartTime(value);
                        break;
                    case "任务结束时刻":
                        stats.setTaskEndTime(value);
                        break;
                    case "任务总计耗时":
                        stats.setTaskTotalTime(value);
                        break;
                    case "任务平均流量":
                        stats.setTaskAverageFlow(value);
                        break;
                    case "记录写入速度":
                        stats.setTaskRecordWritingSpeed(value);
                        break;
                    case "读出记录总数":
                        stats.setTaskRecordReaderNum(value);
                        break;
                    case "读写失败总数":
                        stats.setTaskRecordWriteFailNum(value);
                        break;
                    default:
                        // 非统计行忽略
                        break;
                }
            }
        }
        return stats;
    }
}
