package com.data.connector.plugin;

import com.data.profile.common.enums.DataType;
import com.data.profile.common.utils.StringUtils;

public class ClickHouseTypeConverter extends JdbcTypeConverter {
    @Override
    public DataType convert(String originType) {
        if (StringUtils.isEmpty(originType)) {
            throw new UnsupportedOperationException("sql type id null error");
        }

        switch (originType.toUpperCase()) {
            case "DECIMAL32":
            case "DECIMAL64":
                return DataType.BIG_DECIMAL_TYPE;
            default:
                return super.convert(originType);
        }

    }
}
