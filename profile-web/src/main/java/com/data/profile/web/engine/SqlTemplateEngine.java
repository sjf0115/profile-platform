package com.data.profile.web.engine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * SQL 模板渲染引擎
 * <p>基于 FreeMarker，将 SQL 模板与参数渲染为可执行 SQL</p>
 */
@Slf4j
@Component
public class SqlTemplateEngine {

    private Configuration configuration;

    @PostConstruct
    public void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(getClass(), "/sql-templates");
        configuration.setDefaultEncoding("UTF-8");
    }

    /**
     * 渲染 SQL 模板
     *
     * @param templateName 模板文件名（如 "label_distribution.ftl"）
     * @param params       模板参数
     * @return 渲染后的 SQL 字符串
     */
    public String render(String templateName, Map<String, Object> params) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(params, writer);
            return writer.toString().trim();
        } catch (IOException | TemplateException e) {
            log.error("SQL 模板渲染失败: template={}, params={}", templateName, params, e);
            throw new RuntimeException("SQL 模板渲染失败: " + templateName, e);
        }
    }
}
