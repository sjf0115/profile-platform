package com.data.profile.web.enums;

/**
 * 血缘关系类型
 */
public enum RelationType {
    DERIVE("derive", "派生自"),
    REFERENCE("reference", "引用"),
    CONSUME("consume", "消费"),
    EXPORT("export", "投递自");

    private final String code;
    private final String name;

    RelationType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RelationType of(String code) {
        if (code == null || code.isEmpty()) return null;
        for (RelationType t : values()) {
            if (t.code.equals(code)) return t;
        }
        return null;
    }

    public static String getNameByCode(String code) {
        RelationType t = of(code);
        return t != null ? t.name : code;
    }
}
