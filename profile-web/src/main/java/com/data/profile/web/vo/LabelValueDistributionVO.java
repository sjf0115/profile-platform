package com.data.profile.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 标签取值分布与覆盖量视图对象（标签详情页）
 */
@Data
public class LabelValueDistributionVO {
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
    private List<ItemVO> values;

    @Data
    public static class ItemVO {
        // 标签值
        private String value;
        // 计数
        private Long count;
        // 人次占比（百分比，保留 1 位小数）
        private Double percent;
    }
}
