package com.data.connector.plugin;

import com.data.connector.api.Connector;
import com.data.profile.common.domain.connector.jdbc.DatabaseInfo;
import com.data.profile.common.domain.connector.jdbc.TableInfo;
import com.data.profile.common.domain.connector.request.*;
import com.data.profile.common.utils.JSONUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Kafka Connector
 * <p>实现 Connector 接口，支持 Kafka 元数据浏览（topics/consumer groups）和连接测试。</p>
 * <p>概念映射：topics → databases，consumer groups → tables。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class KafkaConnector implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);

    private static final String BOOTSTRAP_SERVERS = "bootstrapServers";

    @Override
    public ConnectorResponse getDatabases(GetDatabasesRequestParam param) {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        Map<String, String> paramMap = JSONUtils.toMap(param.getDataSourceParam());

        try (AdminClient adminClient = createAdminClient(paramMap)) {
            Collection<TopicListing> topics = adminClient.listTopics().listings().get();
            List<DatabaseInfo> databaseList = new ArrayList<>();
            for (TopicListing topic : topics) {
                databaseList.add(new DatabaseInfo(topic.name(), "topic"));
            }
            builder.result(databaseList);
        } catch (Exception e) {
            logger.error("Kafka list topics error: ", e);
            builder.status(ConnectorResponse.Status.ERROR).errorMsg(e.getMessage());
        }

        return builder.build();
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        Map<String, String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        String topic = param.getDatabase();

        try (AdminClient adminClient = createAdminClient(paramMap)) {
            // 列出消费了该 topic 的 consumer groups
            Collection<ConsumerGroupListing> groups = adminClient.listConsumerGroups().all().get();

            List<TableInfo> tableList = new ArrayList<>();
            for (ConsumerGroupListing group : groups) {
                String groupId = group.groupId();
                // 检查该 consumer group 是否消费了指定 topic
                try {
                    ListConsumerGroupOffsetsResult offsetsResult =
                            adminClient.listConsumerGroupOffsets(groupId);
                    Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets =
                            offsetsResult.partitionsToOffsetAndMetadata().get();
                    for (TopicPartition tp : offsets.keySet()) {
                        if (tp.topic().equals(topic)) {
                            tableList.add(new TableInfo(topic, groupId, "consumer-group", null));
                            break;
                        }
                    }
                } catch (Exception e) {
                    // 忽略单个 group 的查询失败
                    logger.warn("Failed to get offsets for group {}: {}", groupId, e.getMessage());
                }
            }
            builder.result(tableList);
        } catch (Exception e) {
            logger.error("Kafka list consumer groups error: topic={}", topic, e);
            builder.status(ConnectorResponse.Status.ERROR).errorMsg(e.getMessage());
        }

        return builder.build();
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        Map<String, String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        try (AdminClient adminClient = createAdminClient(paramMap)) {
            // 通过 listTopics 验证连通性
            adminClient.listTopics().names().get();
            return ConnectorResponse.builder()
                    .status(ConnectorResponse.Status.SUCCESS)
                    .result(true)
                    .build();
        } catch (Exception e) {
            logger.error("Kafka test connect error: ", e);
            return ConnectorResponse.builder()
                    .status(ConnectorResponse.Status.ERROR)
                    .result(false)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    @Override
    public List<String> keyProperties() {
        return Collections.singletonList(BOOTSTRAP_SERVERS);
    }

    /**
     * 根据参数创建 AdminClient
     */
    private AdminClient createAdminClient(Map<String, String> paramMap) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, paramMap.get(BOOTSTRAP_SERVERS));
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");
        return AdminClient.create(props);
    }
}
