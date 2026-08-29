package com.data.engine.api;

import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.spi.SPI;

/**
 * DI 引擎执行器（提交网关模型）。
 *
 * <p>生命周期：{@link #init} → {@link #submit}（立即返回，不阻塞）→
 * 调用方轮询 {@link #getStatus()} 至终态 → 必要时 {@link #cancel}。</p>
 *
 * <p>平台与引擎解耦：平台只"提交 + 轮询"，不在调用线程同步等待批任务跑完；
 * 引擎侧作业句柄通过 {@link EngineJobStatus#getEngineJobId()} 与业务实例双向映射。</p>
 */
@SPI
public interface DiEngineExecutor {
    /**
     * 初始化：解析引擎私有请求（构建作业配置）。
     */
    void init(ExecutorRequest executorRequest, Configurations configurations) throws Exception;

    /**
     * 提交作业，立即返回引擎侧作业ID（不阻塞）。
     */
    String submit() throws Exception;

    /**
     * 查询作业状态与结果指标（幂等，可重复轮询）。
     */
    EngineJobStatus getStatus() throws Exception;

    /**
     * 取消作业（尽力而为，取消结果通过 {@link #getStatus()} 观察）。
     */
    default void cancel() throws Exception {
        throw new UnsupportedOperationException("当前 DI 引擎不支持取消作业");
    }

    /**
     * 暂停作业（仅支持 checkpoint 的引擎，如 SeaTunnel）。
     */
    default void pause() throws Exception {
        throw new UnsupportedOperationException("当前 DI 引擎不支持暂停：无 checkpoint 机制");
    }

    /**
     * 从上次暂停位点恢复作业（仅支持 checkpoint 的引擎，如 SeaTunnel）。
     */
    default void restore() throws Exception {
        throw new UnsupportedOperationException("当前 DI 引擎不支持恢复：无 savepoint 机制");
    }
}
