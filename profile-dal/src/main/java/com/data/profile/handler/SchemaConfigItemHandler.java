package com.data.profile.handler;

import com.data.profile.model.SchemaConfigItem;

/**
 * 功能：SchemaConfigItemHandler
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/2 22:53
 */
public class SchemaConfigItemHandler extends ListTypeHandler<SchemaConfigItem> {
    // 必须显式调用父类的带参构造函数
    public SchemaConfigItemHandler() {
        super(SchemaConfigItem.class);  // 明确传递具体类型
    }
}
