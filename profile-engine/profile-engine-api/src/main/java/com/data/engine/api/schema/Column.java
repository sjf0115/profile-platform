package com.data.engine.api.schema;

import com.data.profile.common.enums.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {

    /** 列名 */
    private String name;

    /** 中性数据类型 */
    private DataType dataType;

    /** 是否可空，默认 true */
    private boolean nullable;

    /** 是否主键 */
    private boolean primaryKey;

    /** 列注释 */
    private String comment;

    /** 精度（DECIMAL 等需要） */
    private Integer precision;

    /** 小数位数（DECIMAL 等需要） */
    private Integer scale;

    /** 长度（VARCHAR 等需要，可空） */
    private Integer length;
}
