package com.data.profile.web.vo;

import lombok.Data;

/**
 * 功能：分布项 VO
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
public class DistributionItemVO {
    // 标签值（如"北京"）
    private String value;
    // 当前人群计数
    private Long currentCount;
    // 当前人群占比
    private Double currentRate;
    // 全体人群计数
    private Long allCount;
    // 全体人群占比
    private Double allRate;
    // 对比群组计数（可选）
    private Long compareCount;
    // 对比群组占比（可选）
    private Double compareRate;
}
