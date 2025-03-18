package com.data.profile.common.domain;

import lombok.Data;

import java.util.List;

/**
 * 功能：圈选条件
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/18 22:23
 */
@Data
public class FilterCondition {
    private int type;
    private String id;
    private String name;
    private String op;
    private List<String> value;
}
