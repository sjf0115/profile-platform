package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：分布项聚合传输对象（Service 层出参）
 */
@Data
public class DistributionItemDTO {
    // 标签值（如"北京"）
    private String value;
    // 当前人群计数
    private Long currentCount;
    // 当前人群占比
    private Double currentRate;
    // 对比群组计数（可选）
    private Long compareCount;
    // 对比群组占比（可选）
    private Double compareRate;
}
