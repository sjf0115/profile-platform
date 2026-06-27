package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：群组规则
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/18 00:08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupRule {
    // 规则类型: rule-标签筛选, upload-文件上传, sql-SQL创建
    private String type;
    // 1. 规则标签筛选
    // 标签筛选规则表达式
    private RuleExpression expression;

    // 2. 文件上传方式
    // MinIO 中存储的文件路径
    private String uuidFileKey;
    // 上传的文件列表
    private List<String> fileList;

    // 3. SQL创建方式
    // SQL 语句
    private String sqlText;
}
