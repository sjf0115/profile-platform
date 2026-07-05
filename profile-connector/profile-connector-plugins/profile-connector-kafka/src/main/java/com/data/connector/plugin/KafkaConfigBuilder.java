package com.data.connector.plugin;

import com.data.connector.api.ConfigBuilder;
import com.data.profile.common.domain.connector.CommonConstants;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.PropsType;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.props.InputParamsProps;
import com.data.profile.common.domain.connector.param.type.InputParam;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Kafka 配置表单构建
 * <p>生成前端数据源配置表单：bootstrapServers、topic。</p>
 *
 * 作者：SmartSi
 */
@Slf4j
public class KafkaConfigBuilder implements ConfigBuilder {

    @Override
    public String build() {
        List<PluginParams> params = new ArrayList<>();
        params.add(getBootstrapServersInput());
        params.add(getTopicInput());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;
        try {
            result = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("json parse error: ", e);
        }
        return result;
    }

    @Override
    public String buildErrorDataStorage() {
        // Kafka 不支持作为错误数据存储
        return null;
    }

    private InputParam getBootstrapServersInput() {
        return getInputParam("bootstrapServers",
                "连接地址",
                "请填入 Kafka Bootstrap Servers，如 localhost:9092,localhost:9093", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入连接地址").build(),
                null);
    }

    private InputParam getTopicInput() {
        return getInputParam("topic",
                "Topic",
                "请填入 Topic 名称", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入 Topic").build(),
                null);
    }

    private InputParam getInputParam(String field, String title, String placeholder, int rows, Validate validate, Object defaultValue) {
        return InputParam
                .newBuilder(field, title)
                .addValidate(validate)
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXT)
                .setRows(rows)
                .setPlaceholder(placeholder)
                .setValue(defaultValue)
                .setEmit(null)
                .build();
    }
}
