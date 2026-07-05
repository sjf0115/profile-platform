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
 * MinIO 配置表单构建
 * <p>生成前端数据源配置表单：endpoint、accessKey、secretKey、bucket。</p>
 *
 * 作者：SmartSi
 */
@Slf4j
public class MinioConfigBuilder implements ConfigBuilder {

    @Override
    public String build() {
        List<PluginParams> params = new ArrayList<>();
        params.add(getEndpointInput());
        params.add(getAccessKeyInput());
        params.add(getSecretKeyInput());
        params.add(getBucketInput());

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
        // MinIO 不支持作为错误数据存储
        return null;
    }

    private InputParam getEndpointInput() {
        return getInputParam("endpoint",
                "连接地址",
                "请填入 MinIO 连接地址，如 http://localhost:9000", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入连接地址").build(),
                null);
    }

    private InputParam getAccessKeyInput() {
        return getInputParam("accessKey",
                "用户名",
                "请填入用户名(Access Key)", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入用户名").build(),
                null);
    }

    private InputParam getSecretKeyInput() {
        return getInputParam("secretKey",
                "密码",
                "请填入密码(Secret Key)", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入密码").build(),
                null);
    }

    private InputParam getBucketInput() {
        return getInputParam("bucket",
                "存储桶",
                "请填入存储桶名称（可选）", 1,
                Validate.newBuilder().setRequired(false).setMessage("请填入存储桶").build(),
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
