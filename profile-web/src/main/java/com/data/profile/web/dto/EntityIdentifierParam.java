package com.data.profile.web.dto;

import lombok.Data;

/**
 * 实体标识查询参数
 */
@Data
public class EntityIdentifierParam {
    // 实体标识名称
    private String entityIdentifierName;
    // 所属实体ID
    private String entityId;
    // 状态: 1-启用, 2-停用
    private Integer status;
}
