package com.data.profile.web.engine;

import com.data.profile.web.model.Engine;
import com.data.profile.web.service.EngineService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * SQL 模板渲染引擎
 * <p>基于 FreeMarker，将 SQL 模板与参数渲染为可执行 SQL</p>
 * <p>方言隔离：模板按引擎类型分目录存放（sql-templates/{engineType}/），
 * 渲染时按默认分析引擎自动分派（MyBatis databaseId 模式），
 * 引擎目录缺失时回退跨引擎中性模板目录 common/。</p>
 */
@Slf4j
@Component
public class SqlTemplateEngine {

    /** 分析引擎类别（与 AnalysisEngineService.CATEGORY_ANALYSIS 一致） */
    private static final String CATEGORY_ANALYSIS = "analysis";

    /** 跨引擎中性模板目录（ANSI 语法），引擎专属模板缺失时兜底 */
    private static final String COMMON_DIR = "common";

    @Resource
    private EngineService engineService;

    private Configuration configuration;

    @PostConstruct
    public void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(getClass(), "/sql-templates");
        configuration.setDefaultEncoding("UTF-8");
    }

    /**
     * 渲染 SQL 模板（按默认分析引擎类型目录分派）
     *
     * <p>加载优先级：sql-templates/{engineType}/{templateName}
     * → 缺失回退 sql-templates/common/{templateName} → 均缺失抛异常。</p>
     *
     * @param templateName 模板文件名（如 "label_distribution.ftl"，不含引擎目录前缀）
     * @param params       模板参数
     * @return 渲染后的 SQL 字符串
     */
    public String render(String templateName, Map<String, Object> params) {
        String engineType = resolveEngineType();
        Template template = loadTemplate(engineType + "/" + templateName);
        if (template == null) {
            template = loadTemplate(COMMON_DIR + "/" + templateName);
        }
        if (template == null) {
            throw new RuntimeException("分析引擎 [" + engineType + "] 未提供 SQL 模板 [" + templateName
                    + "]，common 兜底目录亦缺失");
        }
        try {
            StringWriter writer = new StringWriter();
            template.process(params, writer);
            return writer.toString().trim();
        } catch (IOException | TemplateException e) {
            log.error("SQL 模板渲染失败: template={}, params={}", templateName, params, e);
            throw new RuntimeException("SQL 模板渲染失败: " + templateName, e);
        }
    }

    /**
     * 解析默认分析引擎类型（小写，与门面 SPI 路由 pluginName 同源）
     */
    private String resolveEngineType() {
        Engine engine = engineService.getDefaultEngineByCategory(CATEGORY_ANALYSIS);
        if (engine == null) {
            throw new IllegalStateException("未找到可用的分析引擎(analysis)，请在引擎管理中设置默认分析引擎");
        }
        return StringUtils.lowerCase(StringUtils.trimToEmpty(engine.getEngineType()));
    }

    /**
     * 按路径加载模板，不存在返回 null（用于引擎目录 → common 兜底链）
     */
    private Template loadTemplate(String path) {
        try {
            return configuration.getTemplate(path);
        } catch (TemplateNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException("SQL 模板加载失败: " + path, e);
        }
    }
}
