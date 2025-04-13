CREATE TABLE ${tableName} (
<#list columns as col>
    ${col.columnName} ${col.columnType}<#if col.columnComment??> COMMENT '${col.columnComment}'</#if><#sep>,</#sep>
</#list>
<#if partitions??>
<#list partitions as pt>
    ,${pt.columnName} ${pt.columnType}<#if pt.columnComment??> COMMENT '${pt.columnComment}'</#if><#sep>,</#sep>
</#list>
</#if>
)
ENGINE = MergeTree
<#if partitionBy?has_content>
PARTITION BY ${partitionBy}
</#if>
ORDER BY (${orderBy})
SETTINGS index_granularity = 8192;