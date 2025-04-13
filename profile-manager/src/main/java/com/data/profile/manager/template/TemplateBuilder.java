package com.data.profile.manager.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Map;

/**
 * 功能：FreeMarker 通用模板构造器
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/13 15:52
 */
public class TemplateBuilder {
    private String path;
    private String name;
    private Map<String, Object> params;

    public String getPath() {
        return path;
    }

    public TemplateBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public String getName() {
        return name;
    }

    public TemplateBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public TemplateBuilder setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public String build() {
        try {
            // 1. 创建 Configuration 实例
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            String templatePath = TemplateBuilder.class.getClassLoader().getResource("templates/" + path).getPath();
            cfg.setDirectoryForTemplateLoading(new File(templatePath));

            // 2. 加载模板
            Template template = cfg.getTemplate(name);

            // 3. 生成模板
            StringWriter writer = new StringWriter();
            template.process(params, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("生成 FreeMaker 模板失败: " + e.getMessage());
        }
    }
}
