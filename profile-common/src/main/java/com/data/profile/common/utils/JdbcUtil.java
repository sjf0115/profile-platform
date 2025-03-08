package com.data.profile.common.utils;

import com.data.profile.model.JdbcParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

/**
 * 功能：
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/8 23:58
 */
public class JdbcUtil {
    public static String buildUrl(String dataSourceTypeName, JdbcParam jdbcParam) throws URISyntaxException {
        String url = jdbcParam.getUrl();
        if (StringUtils.isNotBlank(url)) {
            return url;
        }
        String host = jdbcParam.getHost();
        int port = jdbcParam.getPort();
        String databaseName = jdbcParam.getDatabaseName();
        Map<String, String> params = jdbcParam.getParams();

        if (Objects.equals(dataSourceTypeName, "hive")) {
            dataSourceTypeName = "hive2";
        }

        // JDBC URL
        URIBuilder uriBuilder =  new URIBuilder();
        uriBuilder.setScheme("jdbc")
                .setHost(host)
                .setPort(port)
                .setPath(databaseName);
        for (String paramKey : params.keySet()) {
            uriBuilder.addParameter(paramKey, params.get(paramKey));
        }
        URI uri = uriBuilder.build();
        return uri.toString();
    }
}
