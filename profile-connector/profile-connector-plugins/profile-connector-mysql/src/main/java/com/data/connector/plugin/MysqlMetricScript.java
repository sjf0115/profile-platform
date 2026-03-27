package com.data.connector.plugin;

public class MysqlMetricScript extends JdbcMetricScript {

    @Override
    public String histogramActualValue(String uniqueKey, String where) {
        return "select concat(k, '\001', cast(count as char)) as actual_value_" +
                uniqueKey + " from (select if(${column} is null, 'NULL', cast(${column} as char)) as k, count(1) as count from ${table} " +
                where + " group by ${column} order by count desc limit 50) T";
    }
}
