package com.data.profile.web.dto;

import lombok.Data;

import java.util.List;

/**
 * 功能：群组分析请求 DTO
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
public class GroupAnalysisRequest {
    // 当前群组ID
    private String groupId;
    // 要分析的标签ID（单标签）
    private String labelId;
    // 对比群组ID列表（可选）
    private List<String> compareGroupIds;
}
