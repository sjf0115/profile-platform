package com.data.engine.api.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Schema 差异（current vs target）。
 *
 * <p>由 {@link EngineTableManager#diff(TableSchema, TableSchema)} 计算，
 * 由 {@link EngineTableManager#alterTable(TableSchema, SchemaDiff)} 应用。</p>
 *
 * <p>P1 阶段仅支持 add / drop / modify 三类原子操作；
 * 重命名（rename）需要业务侧明确语义，暂不支持。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDiff {

    /** 新增列 */
    private List<Column> addColumns;

    /** 待删除列名 */
    private List<String> dropColumns;

    /** 类型 / 注释变更（新定义） */
    private List<Column> modifyColumns;

    public List<Column> getAddColumns() {
        return addColumns == null ? Collections.emptyList() : addColumns;
    }

    public List<String> getDropColumns() {
        return dropColumns == null ? Collections.emptyList() : dropColumns;
    }

    public List<Column> getModifyColumns() {
        return modifyColumns == null ? Collections.emptyList() : modifyColumns;
    }

    /** 是否无任何变更。 */
    public boolean isEmpty() {
        return getAddColumns().isEmpty()
                && getDropColumns().isEmpty()
                && getModifyColumns().isEmpty();
    }
}
