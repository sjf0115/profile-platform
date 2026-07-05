package com.data.profile.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 功能：标签分布 VO
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
public class LabelDistributionVO {
    // 标签ID
    private String labelId;
    // 标签名称
    private String labelName;
    // 来源数据集名称
    private String datasetName;
    // 更新方式描述
    private String updateType;
    // 分布项列表
    private List<DistributionItemVO> values;
}
