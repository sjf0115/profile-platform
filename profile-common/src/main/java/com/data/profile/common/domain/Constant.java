package com.data.profile.common.domain;

/**
 * 功能：常量
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/2/16 17:52
 */
public class Constant {
    public static String DEFAULT_LABEL_CATEGORY = "未分类";
    public static final String TOKEN = "token";
    public static final String USER_ID = "id";
    public static final String SESSION_USER_CONTEXT = "session.user.context";

    public static final String ENGINE_SEATUNNEL = "seatunnel";
    public static final String ENGINE_CLICKHOUSW = "clickhouse";

    public static final String AUTHENTICATION_PROVIDER_PASSWORD = "PASSWD";
    public static final String AUTHENTICATION_PROVIDER_LDAP = "LDAP";

    // 引擎相关常量
    public static final String ENGINE_DATASET_TABLE_PREFIX = "profile_dataset_";
    public static final String ENGINE_GROUP_TABLE_PREFIX = "profile_group_";
    public static final String ENGINE_LABEL_TABLE_PREFIX = "profile_label_";

    public static final String ENGINE_LABEL_TABLE_ENTITY_COLUMN = "entity_id"; // 标签表主体列
    public static final String ENGINE_LABEL_TABLE_VALUE_COLUMN = "label_value"; // 标签表标签值列

    public static final String ENGINE_CATEGORY_DI = "di"; // 同步引擎
    public static final String ENGINE_CATEGORY_ANALYSIS = "analysis"; // 分析引擎
    public static final String ENGINE_GROUP_TABLE_ENTITY = "entity_id"; // 分析引擎群组表实体ID字段名称

    // 默认角色ID
    public static final String DEFAULT_ADMIN_ROLE_ID = "1800000000000000";
    public static final String DEFAULT_ANALYST_ROLE_ID = "1800000000000001";
    public static final String DEFAULT_VISITOR_ROLE_ID = "1800000000000002";
}
