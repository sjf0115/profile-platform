package com.data.profile.web.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 功能：群组分析 VO
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
public class GroupAnalysisVO {
    // 分析ID
    private String analysisId;
    // 分析名称
    private String analysisName;
    // 分析描述
    private String analysisDesc;
    // 当前群组ID
    private String groupId;
    // 当前群组名称（关联查询）
    private String groupName;
    // 当前群组规模（关联查询）
    private Integer groupCount;
    // 当前群组实体类型（关联查询）
    private String entityIdentifierName;
    private String entityName;
    // 群组创建方式（关联查询）
    private Integer groupType;
    // 群组状态（关联查询）
    private Integer groupStatus;
    // 对比群组ID列表
    private List<String> compareGroupIds;
    // 已选标签ID列表
    private List<String> labelIds;
    // 状态
    private Integer status;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
