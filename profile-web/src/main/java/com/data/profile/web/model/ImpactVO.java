package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 影响分析 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactVO {
    /** 下游节点类型 */
    private String nodeType;
    /** 下游节点ID */
    private String nodeId;
    /** 下游节点名称 */
    private String nodeName;
    /** 距源节点跳数 */
    private int depth;
}
