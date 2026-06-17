package com.data.profile.web.task;

import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import lombok.Builder;
import lombok.Data;

/**
 * 任务执行上下文
 * <p>封装执行所需的全部信息：任务定义、任务实例、关联业务实体ID。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
@Builder
public class ExecutionContext {
    /**
     * 任务定义
     */
    private Task task;

    /**
     * 任务实例
     */
    private TaskInstance instance;

    /**
     * 关联业务实体ID（如 datasetId, groupId）
     */
    private String relatedId;
}
