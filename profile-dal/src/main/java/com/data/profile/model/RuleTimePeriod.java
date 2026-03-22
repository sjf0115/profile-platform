package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：时间周期
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleTimePeriod {
    // 快捷时间：今天/昨天/本周/上周/本月/上月/今年/去年/过去5天/过去7天/过去14天/过去30天/过去60天/过去90天/过去180天/自定义
    private int type;
    // 开始毫秒时间戳
    private Long beginTimestamp;
    // 结束毫秒时间戳
    private Long endTimestamp;
    // 粒度: year、month、day、hour、minute、second
    private String unit;
    // 周期度量
    private int amount;
}
