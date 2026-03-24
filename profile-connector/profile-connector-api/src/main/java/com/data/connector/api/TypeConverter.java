package com.data.connector.api;

import com.data.profile.common.enums.DataType;

public interface TypeConverter {

    DataType convert(String originType);

    String convertToOriginType(DataType dataType);
}
