package com.data.profile.manager.dao.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 功能：分析引擎-投递
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/30 11:57
 */
public class ExportDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 群组表名：G{groupId}

    // 根据群组 ID 查询群组对应表用户规模
    public Integer groupSize(Long groupId) {
        String sql = "SELECT COUNT(*) FROM G?";
        return jdbcTemplate.queryForObject(sql, Integer.class, groupId);
    }

    // 判断群组中是否包含对应的用户
    public boolean contain(Long groupId, Long userId) {
        String sql = "SELECT COUNT(*) FROM G? WHERE id = ?";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, groupId, userId);
        return result > 0;
    }

    public void exportGroup(String groupId) {
        // 1. 通过 jdbcTemplate 查询群组明细
        String sql = "SELECT id FROM " + groupId + "GROUP BY id";
        jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {

            }
        });
        // 2. 批量写入都目标数据源
    }


    //------------------------------------------------------------------------------------------------------------------
    private void exportGroupToTable() {
        // 通过 Jdbc 批量写入到数据库表
        // 数据库、数据表
    }

    private void exportGroupToOSS() {

    }
}
