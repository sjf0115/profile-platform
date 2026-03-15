package com.data.profile.manager.template;

import com.data.profile.manager.domain.Column;
import com.data.profile.manager.utils.TemplateBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * 功能：
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/17 23:13
 */
public class TemplateBuilderTest {
    @Test
    public void builderTest() {

        List<Column> columns = Lists.newArrayList();

        Column dtColumn = new ColumnBuilder()
                .setColumnName("dt")
                .setColumnType("String")
                .setColumnComment("日期")
                .build();
        columns.add(dtColumn);

        Column ageColumn = new ColumnBuilder()
                .setColumnName("age")
                .setColumnType("Int64")
                .setColumnComment("年龄")
                .build();
        columns.add(ageColumn);

        Column sexColumn = new ColumnBuilder()
                .setColumnName("sex")
                .setColumnType("String")
                .setColumnComment("性别")
                .build();
        columns.add(sexColumn);


        Map<String, Object> params = Maps.newHashMap();
        params.put("tableName", "user_profile");
        params.put("columns", columns);
        params.put("orderBy", "dt");

        String template = new TemplateBuilder()
                .setPath("clickhouse")
                .setName("create_table.ftl")
                .setParams(params)
                .build();
        System.out.println(template);
    }
}
