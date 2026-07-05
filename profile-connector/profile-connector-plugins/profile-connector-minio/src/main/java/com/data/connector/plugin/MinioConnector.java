package com.data.connector.plugin;

import com.data.connector.api.Connector;
import com.data.profile.common.domain.connector.jdbc.DatabaseInfo;
import com.data.profile.common.domain.connector.jdbc.TableInfo;
import com.data.profile.common.domain.connector.request.*;
import com.data.profile.common.utils.JSONUtils;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * MinIO 对象存储 Connector
 * <p>实现 Connector 接口，支持 MinIO 元数据浏览（buckets/objects）和连接测试。</p>
 * <p>概念映射：buckets → databases，objects → tables。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class MinioConnector implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(MinioConnector.class);

    private static final String ENDPOINT = "endpoint";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";

    @Override
    public ConnectorResponse getDatabases(GetDatabasesRequestParam param) {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        Map<String, String> paramMap = JSONUtils.toMap(param.getDataSourceParam());

        try {
            MinioClient client = createMinioClient(paramMap);
            List<Bucket> buckets = client.listBuckets();
            List<DatabaseInfo> databaseList = new ArrayList<>();
            for (Bucket bucket : buckets) {
                databaseList.add(new DatabaseInfo(bucket.name(), "bucket"));
            }
            builder.result(databaseList);
        } catch (Exception e) {
            logger.error("MinIO list buckets error: ", e);
            builder.status(ConnectorResponse.Status.ERROR).errorMsg(e.getMessage());
        }

        return builder.build();
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        Map<String, String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        String bucket = param.getDatabase();

        try {
            MinioClient client = createMinioClient(paramMap);
            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder().bucket(bucket).build());

            List<TableInfo> tableList = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                tableList.add(new TableInfo(bucket, item.objectName(), "object", null));
            }
            builder.result(tableList);
        } catch (Exception e) {
            logger.error("MinIO list objects error: bucket={}", bucket, e);
            builder.status(ConnectorResponse.Status.ERROR).errorMsg(e.getMessage());
        }

        return builder.build();
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        Map<String, String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        try {
            MinioClient client = createMinioClient(paramMap);
            // 通过 listBuckets 验证连通性
            client.listBuckets();
            return ConnectorResponse.builder()
                    .status(ConnectorResponse.Status.SUCCESS)
                    .result(true)
                    .build();
        } catch (Exception e) {
            logger.error("MinIO test connect error: ", e);
            return ConnectorResponse.builder()
                    .status(ConnectorResponse.Status.ERROR)
                    .result(false)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    @Override
    public List<String> keyProperties() {
        return Arrays.asList(ENDPOINT, ACCESS_KEY, SECRET_KEY);
    }

    /**
     * 根据参数创建 MinioClient
     */
    private MinioClient createMinioClient(Map<String, String> paramMap) {
        String endpoint = paramMap.get(ENDPOINT);
        String accessKey = paramMap.get(ACCESS_KEY);
        String secretKey = paramMap.get(SECRET_KEY);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
