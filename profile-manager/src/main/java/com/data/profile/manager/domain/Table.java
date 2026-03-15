package com.data.profile.manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：数据库表
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/6 13:38
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    // 表名
    private String tableName;
    // 表备注
    private String tableComment;
    // 是否是分区表
    private boolean isPartitionTable;
    // 表字段
    private List<Column> columns;
}
