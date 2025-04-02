package com.data.profile.handler;

import com.data.profile.model.SchemaConfigItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 功能：ListTypeHandler
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/2 22:53
 */
public class SchemaConfigItemHandler extends BaseTypeHandler<List<SchemaConfigItem>> {
    private final Gson gson = new Gson();
    private final Type type = new TypeToken<List<SchemaConfigItem>>() {}.getType();

    /**
     * 构造函数：通过泛型类型初始化
     * @param clazz
     */
//    public ListTypeHandler(Class<T> clazz) {
//        this.type = TypeToken.getParameterized(List.class, clazz).getType();
//    }

    /**
     * 序列化：Java对象 → JSON字符串（写入数据库）
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<SchemaConfigItem> parameter, JdbcType jdbcType) throws SQLException {
        String json = gson.toJson(parameter);
        ps.setString(i, json);
    }

    /**
     * 反序列化：JSON字符串 → Java对象（从数据库读取）
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    @Override
    public List<SchemaConfigItem> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<SchemaConfigItem> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<SchemaConfigItem> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    /**
     * 解析Json
     * @param json
     * @return
     */
    private List<SchemaConfigItem> parseJson(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        return gson.fromJson(json, type);
    }
}
