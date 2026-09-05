package com.data.profile.web.dto;

import lombok.Data;

import java.util.List;

/**
 * 功能：标签取值分布与覆盖量聚合传输对象（Service 层出参，标签详情页维度）
 */
@Data
public class LabelValueDistributionDTO {
    // 标签ID
    private String labelId;
    // 覆盖量（字段非空行数）
    private Long coverCount;
    // 引擎表总行数
    private Long totalCount;
    // 覆盖率（百分比，保留 1 位小数）
    private Double coverRate;
    // 标签样例（分布 Top1 值）
    private String sampleValue;
    // 是否有数据（标签未绑定或引擎表未就绪时为 false）
    private Boolean hasData;
    // 分布项列表（Top10）
    private List<Item> values;

    @Data
    public static class Item {
        // 标签值
        private String value;
        // 计数
        private Long count;
        // 人次占比（百分比，保留 1 位小数）
        private Double percent;
    }
}
