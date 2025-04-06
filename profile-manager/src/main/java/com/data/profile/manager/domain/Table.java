package com.data.profile.manager.domain;

import lombok.Data;

/**
 * 功能：数据库表
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/6 13:38
 */
@Data
public class Table {
    private String name;
    private String comment;
    private boolean isPartitionTable;
}
