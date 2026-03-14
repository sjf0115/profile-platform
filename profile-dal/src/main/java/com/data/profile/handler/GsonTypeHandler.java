package com.data.profile.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 功能：GsonTypeHandler
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/2 22:53
 */
public class GsonTypeHandler<T> extends BaseTypeHandler<T> {
    private final Gson gson = new GsonBuilder().create();
    private final Type type;

    /**
     * 通过子类构造时传入具体类型（例如 new GsonTypeHandler<List<User>>() {}）
     */
    public GsonTypeHandler() {
        // 通过反射获取子类的泛型参数类型
        this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 构造函数：通过泛型类型初始化
     * @param clazz
     */
    public GsonTypeHandler(Class<T> clazz) {
        this.type = clazz;
    }

    /**
     * 手动指定 Java 类型（适用于泛型，如 List<User>）
     */
    public GsonTypeHandler(Type type) {
        this.type = type;
    }

    /**
     * 序列化：Java对象 → JSON字符串（写入数据库）
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
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
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    /**
     * 解析Json
     * @param json
     * @return
     */
    private T parseJson(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return gson.fromJson(json, type);
    }
}
