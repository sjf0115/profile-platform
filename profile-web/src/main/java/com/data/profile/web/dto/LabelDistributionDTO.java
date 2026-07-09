package com.data.profile.web.dto;

import lombok.Data;

import java.util.List;

/**
 * 功能：标签分布聚合传输对象（Service 层出参）
 */
@Data
public class LabelDistributionDTO {
    // 标签ID
    private String labelId;
    // 标签名称
    private String labelName;
    // 来源数据集名称
    private String datasetName;
    // 更新方式描述
    private String updateType;
    // 分布项列表
    private List<DistributionItemDTO> values;
}
