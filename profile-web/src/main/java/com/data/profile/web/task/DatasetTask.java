package com.data.profile.web.task;

import com.data.profile.web.dto.ScheduleConfigRequest;
import com.data.profile.web.engine.DiEngineService;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.model.Task;
import com.data.profile.web.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 功能：数据集定时调度任务
 * <p>负责数据集独立的执行逻辑：数据同步</p>
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class DatasetTask {
    @Resource
    private DiEngineService diEngineService;
    @Resource
    private ScheduleEngineService scheduleEngineService;
    @Resource
    private TaskService taskService;

    /**
     * 执行数据集同步
     * @param datasetId 数据集ID
     */
    public void executeSync(String datasetId) throws Exception {
        log.info("开始执行数据集 [{}] 同步", datasetId);
        diEngineService.executeDatasetSync(datasetId);
    }

    /**
     * TODO 是否放在 DatasetService
     * 配置数据集调度
     */
    public void schedule(String datasetId, ScheduleConfigRequest config) {
        Task task = taskService.getDetailByRelatedId(datasetId);
        if (task == null) {
            log.error("数据集 [{}] 没有关联的同步任务，无法配置调度", datasetId);
            throw new RuntimeException("数据集没有关联的同步任务，请先创建数据集");
        }
        scheduleEngineService.configureSchedule(
                task.getTaskId(),
                config.getTriggerType(),
                config.getTriggerCron(),
                config.getTriggerStartTime(),
                config.getTriggerEndTime());
    }

    /**
     * TODO 是否放在 DatasetService
     * 获取数据集关联的调度任务配置
     */
    public Task getSchedulerConfig(String datasetId) {
        return taskService.getDetailByRelatedId(datasetId);
    }
}
