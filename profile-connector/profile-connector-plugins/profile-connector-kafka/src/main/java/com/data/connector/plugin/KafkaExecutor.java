package com.data.connector.plugin;

import com.data.connector.api.Executor;
import com.data.profile.common.domain.connector.request.ConnectorResponse;
import com.data.profile.common.domain.connector.request.ExecuteRequestParam;
import com.data.profile.common.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Kafka Executor
 * <p>实现消息发送能力（投递/数据管道场景）：每行数据序列化为一条 JSON 消息，
 * 批量发送到目标 topic（param.tableName 承载 topic），同步等待发送结果。</p>
 */
public class KafkaExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(KafkaExecutor.class);

    private static final String BOOTSTRAP_SERVERS = "bootstrapServers";

    @Override
    public ConnectorResponse insertData(ExecuteRequestParam param) throws Exception {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String topic = param.getTableName();
        List<Map<String, Object>> rows = param.getRows();
        if (StringUtils.isEmpty(topic) || rows == null || rows.isEmpty()) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("insertData 需要 topic(tableName) 与 rows");
            return builder.build();
        }

        Map<String, String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        String bootstrapServers = paramMap == null ? null : paramMap.get(BOOTSTRAP_SERVERS);
        if (StringUtils.isEmpty(bootstrapServers)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("Kafka 数据源配置缺少 " + BOOTSTRAP_SERVERS);
            return builder.build();
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 保证消息不丢失：等待所有副本确认
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        long sent = 0;
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            // 先异步提交整批，再统一等待结果（提升吞吐）
            List<Future<RecordMetadata>> futures = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                futures.add(producer.send(new ProducerRecord<>(topic, null, JSONUtils.toJsonString(row))));
            }
            for (Future<RecordMetadata> future : futures) {
                future.get();
                sent++;
            }
        } catch (Exception e) {
            logger.error("Kafka 消息发送失败: topic={}, sent={}", topic, sent, e);
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("Kafka 消息发送失败: " + e.getMessage());
            return builder.build();
        }
        logger.info("Kafka 消息发送完成: topic={}, rows={}", topic, sent);
        builder.result(sent);
        return builder.build();
    }
}
