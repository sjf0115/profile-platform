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
    // 标签筛选规则表达式（type=rule 时使用）
    private RuleExpression expression;
    // MinIO 中存储的文件路径（type=upload 时使用）
    private String uuidFileKey;
    // 上传的文件列表（type=upload 时使用）
    private List<String> fileList;
    // SQL 语句（type=sql 时使用）
    private String sqlText;
}
