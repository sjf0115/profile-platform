package com.data.connector.api.entity;

import com.data.profile.common.enums.DataType;
import lombok.Data;

@Data
public class StructField {

    private String name;

    private DataType dataType;

    private boolean nullable;

    private String comment;
}
