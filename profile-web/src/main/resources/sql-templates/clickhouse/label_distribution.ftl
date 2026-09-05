SELECT d.${fieldName} AS label_value, COUNT(*) AS cnt
FROM ${datasetTable} d
INNER JOIN ${groupTable} g ON toString(g.entity_id) = toString(d.${entityField})
GROUP BY d.${fieldName}
