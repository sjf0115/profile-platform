package com.data.profile.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 功能：群组分析实体
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
public class GroupAnalysis {
    private Long id;
    // 状态: 1-启用, 2-停用
    private Integer status;
    // 分析ID
    private String analysisId;
    // 分析名称
    private String analysisName;
    // 分析描述
    private String analysisDesc;
    // 当前群组ID
    private String groupId;
    // 对比群组ID列表（JSON数组，JSON为String存储）
    @JsonIgnore
    private String compareGroupIds;
    // 已选标签ID列表（JSON数组，JSON字符串存储）
    @JsonIgnore
    private String labelIds;
    // 前端群组ID列表（请求体用）
    @JsonProperty("compare_group_ids")
    private List<String> compareGroupIdList;
    // 标签ID列表（请求体用）
    @JsonProperty("label_ids")
    private List<String> labelIdList;
    // 创建方式: 1-系统内置, 2-自定义
    private Integer sourceType;
    // 负责人
    private String owner;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
