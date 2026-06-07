package com.data.engine.plugin.datax.plugin.jdbc;

/**
 * 功能：示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/6/7 22:43
 */
public class MysqlWriterBuilder extends AbstractJdbcWriterBuilder{
    @Override
    public String getCategory() {
        return "mysql";
    }

    @Override
    public String getPluginName() {
        return "mysqlwriter";
    }

    @Override
    protected String defaultWriteMode() {
        return "insert";
    }
}
