package com.data.engine.api;

import com.data.engine.common.ExecutorRequest;

/**
 * DI 引擎请求构建器：把中性 {@link SyncContext} 转换为目标引擎可消费的 {@link ExecutorRequest}。
 *
 * <p>这是 {@link DiEngineFactory} 的子组件，不作为顶层 SPI；
 * 与 {@link DiEngineExecutor} 协作完成 "构建 + 执行" 两阶段同步任务。</p>
 *
 * <p>设计参考：Apache Linkis EngineConnPlugin 的 EngineConnLaunchBuilder 模式。</p>
 */
public interface DiRequestBuilder {

    /**
     * 把中性同步上下文转换为目标引擎可消费的 ExecutorRequest。
     *
     * @param context 业务侧准备好的同步上下文（不包含任何引擎私有概念）
     * @return 引擎执行器可直接 init 的 ExecutorRequest
     */
    ExecutorRequest buildRequest(SyncContext context);
}
