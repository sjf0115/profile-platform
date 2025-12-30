package com.data.profile.manager.service.scheduler;

import com.data.profile.common.enums.SchedulerJobType;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 功能：调度任务服务类
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/6 14:15
 */
@Service
public class SchedulerService {
    private final static String baseClass = "com.data.profile.manager.service.scheduler.job";
    @Autowired
    private Scheduler scheduler;

    /**
     * 创建调度任务
     * @param jobType
     * @param jobName
     * @param cron
     * @throws SchedulerException
     */
    public void createJob(SchedulerJobType jobType, String jobName, String cron) throws SchedulerException {
        String jobClassName = baseClass + jobType.getName();
        // 1. 获取任务执行类
        Class<? extends Job> jobClass;
        try {
            jobClass = (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new SchedulerException("无法获取调度任务执行类", e);
        }

        // 2. 任务实例
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobType.getName())  // 唯一标识
                .storeDurably()  // 持久化
                .build();

        // 3. 触发器
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_trigger", jobType.getName())
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();

        // 4. 设置任务参数
        JobDataMap dataMap = jobDetail.getJobDataMap();
        dataMap.put("jobName", jobName);

        // 5. 将任务和触发器注册到调度器
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 暂停调度任务
     * @param jobType
     * @param jobName
     * @throws SchedulerException
     */
    public void pauseJob(SchedulerJobType jobType, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobType.getName());
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复调度任务
     * @param jobType
     * @param jobName
     * @throws SchedulerException
     */
    public void resumeJob(SchedulerJobType jobType, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobType.getName());
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除调度任务
     * @param jobType
     * @param jobName
     * @throws SchedulerException
     */
    public void deleteJob(SchedulerJobType jobType, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobType.getName());
        scheduler.deleteJob(jobKey);
    }

    /**
     * 更新调度任务 -- Cron 表达式
     * @param jobType
     * @param jobName
     * @param cron
     * @throws SchedulerException
     */
    public void rescheduleJob(SchedulerJobType jobType, String jobName, String cron) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger", jobType.getName());
        // 旧触发器
        CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (oldTrigger == null) {
            throw new SchedulerException("触发器不存在,无法更新");
        }

        // 新触发器
        CronTrigger newTrigger = oldTrigger.getTriggerBuilder()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();

        // 替换触发器
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }
}
