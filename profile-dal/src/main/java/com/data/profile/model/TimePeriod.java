package com.data.profile.model;

import lombok.Data;

import java.util.List;

/**
 * 功能：时间周期
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:31
 */
@Data
public class TimePeriod {
    // 时间类型: 绝对时间 absolute_time / 相对时间 relative_time
    private String type;
    // 周期: 快捷时间 past_range、时间范围 timestamp
    private String period;
    // 粒度: year、month、day、hour、minute、second
    private String unit;
    // 周期度量
    private int amount;
    // 时间戳 开始时间戳和结束时间戳
    private List<Long> timestamp;
}
