package com.data.profile.web.enums;

import lombok.Getter;

/**
 * 资产节点类型
 */
@Getter
public enum AssetType {
    DATASOURCE("datasource", "数据源"),
    DATASET("dataset", "数据集"),
    LABEL("label", "标签"),
    EVENT("event", "事件"),
    GROUP("group", "群组"),
    ANALYSIS("analysis", "分析"),
    EXPORT("export", "投递"),
    APPLICATION("application", "应用");

    private final String code;
    private final String name;

    AssetType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AssetType of(String code) {
        if (code == null || code.isEmpty()) return null;
        for (AssetType t : values()) {
            if (t.code.equals(code)) return t;
        }
        return null;
    }

    public static String getNameByCode(String code) {
        AssetType t = of(code);
        return t != null ? t.name : code;
    }
}
