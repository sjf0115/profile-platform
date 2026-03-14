package com.data.profile.manager.service.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 功能：投递任务
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/6 12:03
 */
@Slf4j
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ExportJob extends QuartzJobBean {
    @Autowired
    //private ExportService exportService;

    // 投递任务
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 从 JobDataMap 获取执行次数
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        int count = jobDataMap.getInt("count");
        // 执行具体的业务逻辑
        log.info("Welcome to Quartz: {}", count);
        jobDataMap.put("count", count+1);

        // 具体执行的投递逻辑
        // 需要知道投递的群组 group_id 以及目标数据源 datasource_id 和写入的目标对象 object
        // 投递
        //exportService.export();
    }
}
