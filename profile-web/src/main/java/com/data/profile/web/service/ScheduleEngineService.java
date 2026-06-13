package com.data.profile.web.service;

import com.data.engine.api.ScheduleContext;
import com.data.engine.api.ScheduleEngineFactory;
import com.data.engine.api.ScheduleExecutor;
import com.data.engine.api.ScheduleTaskRegistrar;
import com.data.profile.common.enums.SchedulerType;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.model.Engine;
import com.data.profile.web.model.Task;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

/**
 * 调度引擎服务。
 *
 * <p>参照 {@link AnalysisEngineService} / {@link DiEngineService} 的模式，
 * 通过 Engine 表 + SPI 插件实现调度能力的可插拔。</p>
 *
 * <p>职责：调度配置的完整流程（Task 元数据更新 + 调度引擎同步）。</p>
 *
 * 作者：@SmartSi
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class ScheduleEngineService {

    private static final String CATEGORY_SCHEDULE = "schedule";

    @Resource
    private EngineService engineService;

    @Resource
    private TaskService taskService;

    /**
     * 配置调度（一站式业务方法）。
     *
     * <p>完整流程：获取调度引擎 → 加载 SPI 插件 → 注册/更新调度 → 上线/下线 → 更新 Task 元数据。</p>
     *
     * @param taskId 任务ID
     * @param triggerType 调度类型: 1-手动触发, 3-日周期, 4-小时周期
     * @param cron Cron 表达式（周期调度时必填）
     * @param startTime 生效开始时间
     * @param endTime 生效结束时间
     */
    public void configureSchedule(String taskId, int triggerType, String cron,
                                   String startTime, String endTime) {
        Task task = taskService.getDetail(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        // 1. 获取默认调度引擎
        Engine scheduleEngine = getDefaultScheduleEngine();

        // 2. 加载 SPI 插件
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(scheduleEngine.getEngineType()));
        ScheduleEngineFactory factory = PluginLoader
                .getPluginLoader(ScheduleEngineFactory.class)
                .getOrCreatePlugin(pluginName);

        // 3. 构建 ScheduleContext
        ScheduleContext context = buildScheduleContext(task, triggerType, cron, startTime, endTime, scheduleEngine);

        // 4. 获取 TaskRegistrar 并注册/更新调度
        ScheduleTaskRegistrar registrar = factory.getTaskRegistrar();
        registrar.init(parseConfig(scheduleEngine.getConfig()));

        if (StringUtils.isBlank(task.getScheduleId())) {
            // 首次：注册调度
            String scheduleId = registrar.register(context);
            task.setScheduleId(scheduleId);
            log.info("调度引擎注册成功: taskId={}, scheduleId={}", taskId, scheduleId);
        } else {
            // 非首次：更新调度
            registrar.update(task.getScheduleId(), context);
            log.info("调度引擎更新成功: taskId={}, scheduleId={}", taskId, task.getScheduleId());
        }

        // 5. 上线/下线
        if (triggerType == SchedulerType.MANUAL.getCode()) {
            // 手动触发：下线调度（仅保留 Workflow 定义，不自动执行）
            if (StringUtils.isNotBlank(task.getScheduleId())) {
                registrar.offline(task.getScheduleId());
            }
        } else {
            // 周期调度：上线
            registrar.online(task.getScheduleId());
        }

        // 6. 更新 Task 元数据
        task.setTriggerType(triggerType);
        task.setTriggerCron(cron);
        task.setTriggerStartTime(startTime);
        task.setTriggerEndTime(endTime);
        taskService.update(task);
    }

    /**
     * 手动触发调度执行。
     *
     * @param taskId 任务ID
     */
    public void triggerSchedule(String taskId) {
        Task task = taskService.getDetail(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        if (StringUtils.isBlank(task.getScheduleId())) {
            throw new RuntimeException("任务未注册到调度引擎: " + taskId);
        }

        Engine scheduleEngine = getDefaultScheduleEngine();
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(scheduleEngine.getEngineType()));
        ScheduleEngineFactory factory = PluginLoader
                .getPluginLoader(ScheduleEngineFactory.class)
                .getOrCreatePlugin(pluginName);

        ScheduleExecutor executor = factory.getScheduler();
        executor.init(parseConfig(scheduleEngine.getConfig()));
        executor.trigger(task.getScheduleId());
        log.info("调度引擎触发执行: taskId={}, scheduleId={}", taskId, task.getScheduleId());
    }

    /**
     * 删除调度引擎中的调度定义。
     *
     * @param taskId 任务ID
     */
    public void deleteSchedule(String taskId) {
        Task task = taskService.getDetail(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        if (StringUtils.isBlank(task.getScheduleId())) {
            return;
        }

        Engine scheduleEngine = getDefaultScheduleEngine();
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(scheduleEngine.getEngineType()));
        ScheduleEngineFactory factory = PluginLoader
                .getPluginLoader(ScheduleEngineFactory.class)
                .getOrCreatePlugin(pluginName);

        ScheduleTaskRegistrar registrar = factory.getTaskRegistrar();
        registrar.init(parseConfig(scheduleEngine.getConfig()));
        registrar.delete(task.getScheduleId());
        log.info("调度引擎删除调度: taskId={}, scheduleId={}", taskId, task.getScheduleId());
    }

    // -----------------------------------------------------------------
    // 辅助方法
    // -----------------------------------------------------------------

    /**
     * 获取默认调度引擎，不存在则抛异常。
     */
    private Engine getDefaultScheduleEngine() {
        Engine engine = engineService.getDefaultEngineByCategory(CATEGORY_SCHEDULE);
        if (engine == null) {
            throw new IllegalStateException("未找到可用的调度引擎(schedule)，请在引擎管理中设置默认调度引擎");
        }
        return engine;
    }

    /**
     * 构建 ScheduleContext。
     */
    private ScheduleContext buildScheduleContext(Task task, int triggerType, String cron,
                                                 String startTime, String endTime, Engine scheduleEngine) {
        String triggerTypeStr;
        if (triggerType == SchedulerType.DAY_REPEAT.getCode()) {
            triggerTypeStr = "day_repeat";
        } else if (triggerType == SchedulerType.HOUR_REPEAT.getCode()) {
            triggerTypeStr = "hour_repeat";
        } else {
            triggerTypeStr = "manual";
        }

        // 构建 HTTP 回调 URL：调度引擎触发时回调此地址
        String callbackUrl = buildCallbackUrl(task.getTaskId(), scheduleEngine);

        return ScheduleContext.builder()
                .taskId(task.getTaskId())
                .taskName(task.getTaskName())
                .triggerType(triggerTypeStr)
                .cronExpression(cron)
                .startTime(startTime)
                .endTime(endTime)
                .callbackUrl(callbackUrl)
                .build();
    }

    /**
     * 构建 HTTP 回调 URL。
     * 从调度引擎的 config 中读取 callbackBaseUrl，拼接任务执行路径。
     */
    private String buildCallbackUrl(String taskId, Engine scheduleEngine) {
        Map<String, Object> config = parseConfig(scheduleEngine.getConfig());
        String baseUrl = getString(config, "callbackBaseUrl");
        if (StringUtils.isBlank(baseUrl)) {
            // 默认值
            baseUrl = "http://localhost:8080";
        }
        return baseUrl + "/task/" + taskId + "/execute";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        return JSONUtils.parseObject(json, Map.class);
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
