SELECT COUNT(*) AS total_count, countIf(${fieldName} IS NOT NULL) AS cover_count
FROM ${tableName}
