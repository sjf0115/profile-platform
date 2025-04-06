package com.data.profile.handler;

import com.data.profile.model.DatasetField;

/**
 * 功能：DatasetFieldHandler
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/2 22:53
 */
public class DatasetFieldHandler extends ListTypeHandler<DatasetField> {
    // 必须显式调用父类的带参构造函数
    public DatasetFieldHandler() {
        super(DatasetField.class);  // 明确传递具体类型
    }
}
