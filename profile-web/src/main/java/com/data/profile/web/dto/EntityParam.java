package com.data.profile.web.dto;

import lombok.Data;

/**
 * 实体查询参数
 */
@Data
public class EntityParam {
    // 实体名称
    private String entityName;
    // 状态: 1-启用, 2-停用
    private Integer status;
}
