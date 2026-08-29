package com.data.engine.api;

import com.data.profile.common.domain.engine.ProcessResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DI 引擎作业状态快照。
 *
 * <p>{@link DiEngineExecutor#getStatus()} 的返回值：状态 + 结果指标。
 * 仅终态（SUCCESS/FAILED）时 {@link #result} 保证非空。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EngineJobStatus {

    /** 引擎侧作业ID */
    private String engineJobId;

    /** 作业状态 */
    private EngineJobState state;

    /** 执行结果（行数/耗时/错误信息），运行中为 null */
    private ProcessResult result;

    /** 便捷工厂：运行中 */
    public static EngineJobStatus running(String engineJobId) {
        return EngineJobStatus.builder()
                .engineJobId(engineJobId)
                .state(EngineJobState.RUNNING)
                .build();
    }
}
