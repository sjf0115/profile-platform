package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下游依赖 VO（删除保护提示用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependentVO {
    /** 下游节点类型 */
    private String downstreamType;
    /** 下游节点类型名称 */
    private String downstreamTypeName;
    /** 下游节点ID */
    private String downstreamId;
    /** 关系类型 */
    private String relationType;
    /** 关系类型名称 */
    private String relationTypeName;
}
