package com.data.profile.web.dto;

import lombok.Data;

/**
 * 实体标识创建/编辑请求
 */
@Data
public class EntityIdentifierRequest {
    // 实体标识名称
    private String entityIdentifierName;
    // 所属实体ID
    private String entityId;
}
