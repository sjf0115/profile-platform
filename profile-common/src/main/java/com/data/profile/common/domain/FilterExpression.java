package com.data.profile.common.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 功能：圈选表达式
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/18 22:25
 */
@Data
public class FilterExpression {
    private String logic;
    @SerializedName("expression")
    private List<FilterConditionGroup> groups;
}
