package com.data.profile.web.task.executor;

import com.data.profile.common.enums.TaskType;
import com.data.profile.web.task.ExecutionContext;
import com.data.profile.web.task.ExportTask;
import com.data.profile.web.task.TaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 群组投递执行器
 * <p>负责群组投递任务的执行逻辑。</p>
 * <p>实例生命周期由 TaskExecutionService 统一管理。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Component
public class ExportTaskExecutor implements TaskExecutor {
    @Resource
    private ExportTask exportTask;

    @Override
    public TaskType getType() {
        return TaskType.EXPORT;
    }

    @Override
    public void execute(ExecutionContext context) throws Exception {
        exportTask.executeExport(context.getRelatedId());
    }
}
