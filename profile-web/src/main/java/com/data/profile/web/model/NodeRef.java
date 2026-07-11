package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 血缘节点引用（批量查询入参）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeRef {
    private String type;
    private String id;
    private String name;

    public NodeRef(String type, String id) {
        this.type = type;
        this.id = id;
    }
}
