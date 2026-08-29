package com.data.engine.api;

/**
 * DI 引擎作业状态（状态机）。
 *
 * <p>状态流转：SUBMITTED → RUNNING → SUCCESS / FAILED / CANCELLED。
 * SUCCESS/FAILED/CANCELLED 为终态，调用方轮询 {@link DiEngineExecutor#getStatus()} 至终态结束。</p>
 */
public enum EngineJobState {

    /** 已提交，尚未开始运行 */
    SUBMITTED,

    /** 运行中 */
    RUNNING,

    /** 执行成功（终态） */
    SUCCESS,

    /** 执行失败（终态） */
    FAILED,

    /** 已取消（终态） */
    CANCELLED;

    /** 是否终态 */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
}
