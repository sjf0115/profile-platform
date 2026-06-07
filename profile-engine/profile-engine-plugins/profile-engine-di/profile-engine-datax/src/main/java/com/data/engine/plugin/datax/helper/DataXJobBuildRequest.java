package com.data.engine.plugin.datax.helper;

import com.data.engine.plugin.datax.datasource.DataXDataSource;
import com.data.engine.plugin.datax.plugin.bean.ReaderContext;
import com.data.engine.plugin.datax.plugin.bean.WriterContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 构建 DataX JSON 任务配置的入参。由上层（profile-web）准备并放入 {@code ExecutorRequest.config}，
 * 引擎反序列化后交给 {@link DataXJsonHelper}。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataXJobBuildRequest {

    /** Reader 数据源 */
    private DataXDataSource readerSource;
    /** Reader 上下文 */
    private ReaderContext readerContext;

    /** Writer 数据源 */
    private DataXDataSource writerSource;
    /** Writer 上下文 */
    private WriterContext writerContext;

    /** speed.channel；为 null 时不写入 */
    private Integer settingSpeedChannel;
    /** speed.byte（每通道字节限速）；为 null 时不写入 */
    private Long settingSpeedByte;
    /** speed.record（每通道记录限速）；为 null 时不写入 */
    private Long settingSpeedRecord;
    /** errorLimit.record；为 null 时默认 0 */
    private Integer settingErrorRecord;
    /** errorLimit.percentage；为 null 时不写入 */
    private Double settingErrorPercentage;
}
