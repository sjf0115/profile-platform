package com.data.profile.manager.utils;

import com.data.profile.manager.domain.ConnectionParam;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

/**
 * 功能：JDBC 工具类
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/8 23:58
 */
public class JdbcUtil {
    public static String buildUrl(ConnectionParam param) throws URISyntaxException {
        String url = param.getUrl();
        if (StringUtils.isNotBlank(url)) {
            return url;
        }
        String protocol = param.getProtocol();
        String host = param.getHost();
        int port = param.getPort();
        String databaseName = param.getDatabase();
        Map<String, String> params = param.getParams();
        if (Objects.equals(params, null)) {
            params = Maps.newHashMap();
        }

        // JDBC URL
        URIBuilder uriBuilder =  new URIBuilder();
        uriBuilder.setScheme(protocol)
                .setSchemeSpecificPart(protocol)
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
