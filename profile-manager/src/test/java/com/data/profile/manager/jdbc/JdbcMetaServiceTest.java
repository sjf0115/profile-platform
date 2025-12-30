package com.data.profile.manager.jdbc;

import com.data.profile.manager.domain.ConnectionParam;
import com.data.profile.manager.domain.Table;
import com.data.profile.manager.service.JdbcMetaService;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

/**
 * 功能：JdbcMetaService
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/6 20:57
 */
public class JdbcMetaServiceTest {
    @Test
    public void testGetTables() throws SQLException {
        ConnectionParam param = new ConnectionParam();
        param.setDatabase("test");
        param.setUserName("root");
        param.setPassword("root");
        param.setDriver("com.mysql.cj.jdbc.Driver");
        param.setUrl("jdbc://mysql://localhost:3306/test");
        JdbcMetaService metaService = new JdbcMetaService();
        List<Table> tables = metaService.getTables(param);
        for(Table table : tables) {
            System.out.println(table.getName());
        }
    }
}
