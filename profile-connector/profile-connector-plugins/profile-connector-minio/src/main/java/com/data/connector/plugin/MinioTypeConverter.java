package com.data.connector.plugin;

import com.data.connector.api.TypeConverter;
import com.data.profile.common.enums.DataType;

/**
 * MinIO 类型转换器（stub 实现）
 * <p>MinIO 为对象存储，无数据库类型系统。</p>
 *
 * 作者：SmartSi
 */
public class MinioTypeConverter implements TypeConverter {

    @Override
    public DataType convert(String originType) {
        return DataType.STRING_TYPE;
    }

    @Override
    public String convertToOriginType(DataType dataType) {
        return "STRING";
    }
}
