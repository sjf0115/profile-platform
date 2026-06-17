package com.data.profile.web.task;

import com.data.profile.common.enums.TaskType;

/**
 * 任务执行器接口（策略模式）
 * <p>每种任务类型实现一个 Executor，负责执行逻辑。</p>
 * <p>实例生命周期（创建/成功/失败/时间记录）由 TaskExecutionService 统一管理。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public interface TaskExecutor {

    /**
     * 返回支持的任务类型
     */
    TaskType getType();

    /**
     * 执行任务
     * @param context 执行上下文
     */
    void execute(ExecutionContext context) throws Exception;

    /**
     * 执行成功后的回调（可选）。
     * <p>用于业务副作用，如发送通知、触发下游等。</p>
     */
    default void onSuccess(ExecutionContext context) {}

    /**
     * 执行失败后的回调（可选）。
     * <p>用于失败补偿逻辑。</p>
     */
    default void onFailure(ExecutionContext context, Throwable error) {}
}
