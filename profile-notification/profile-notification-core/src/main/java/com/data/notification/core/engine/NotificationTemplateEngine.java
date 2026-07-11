package com.data.notification.core.engine;

import com.data.notification.api.enums.NotificationTemplate;
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
 * 通知模板渲染引擎
 * <p>基于 FreeMarker，将通知模板与参数渲染为可发送的消息内容</p>
 */
@Slf4j
@Component
public class NotificationTemplateEngine {

    private Configuration configuration;

    @PostConstruct
    public void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(getClass(), "/notification-templates");
        configuration.setDefaultEncoding("UTF-8");
    }

    /**
     * 渲染通知模板
     *
     * @param template 通知模板枚举
     * @param params   模板参数
     * @return 渲染后的字符串
     */
    public String render(NotificationTemplate template, Map<String, Object> params) {
        return render(template.getTemplateName() + ".ftl", params);
    }

    /**
     * 渲染指定模板文件
     *
     * @param templateName 模板文件名（如 "alert.ftl"）
     * @param params       模板参数
     * @return 渲染后的字符串
     */
    public String render(String templateName, Map<String, Object> params) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(params, writer);
            return writer.toString().trim();
        } catch (IOException | TemplateException e) {
            log.error("通知模板渲染失败: template={}, params={}", templateName, params, e);
            throw new RuntimeException("通知模板渲染失败: " + templateName, e);
        }
    }
}
