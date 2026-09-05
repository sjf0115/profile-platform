SELECT ${fieldName} AS label_value, COUNT(*) AS cnt
FROM ${tableName}
WHERE ${fieldName} IS NOT NULL
GROUP BY ${fieldName}
ORDER BY cnt DESC
LIMIT ${limit}
