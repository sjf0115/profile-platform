package com.data.profile.manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：数据库列信息
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/3 22:38
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    // 列名称
    private String columnName;
    // 列类型
    private String columnType;
    // 列备注
    private String columnComment;
    // 1-新增字段:数据集字段没有但原始表列有(标记新增的标识)、2-修改字段:数据集字段和原始表列均有、3-删除字段:数据集字段有但原始表列已经删除(标记删除标识)
    // private int status = 1;
}
