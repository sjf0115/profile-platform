package com.data.profile.manager.dao.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 功能：GroupDao 群组
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/29 12:13
 */
@Repository
public class GroupDao {
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
}
