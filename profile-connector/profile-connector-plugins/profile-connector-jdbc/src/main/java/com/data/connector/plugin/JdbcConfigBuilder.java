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

@Slf4j
public class JdbcConfigBuilder implements ConfigBuilder {

    @Override
    public String build() {
        List<PluginParams> params = new ArrayList<>();
        params.add(getHostInput());
        params.add(getPortInput());
        if (getCatalogInput() != null) {
            params.add(getCatalogInput());
        }

        params.add(getDatabaseInput());

        if (getSchemaInput() != null) {
            params.add(getSchemaInput());
        }

        params.add(getUserInput());
        params.add(getPasswordInput());
        params.add(getPropertiesInput());

        params.addAll(getOtherParams());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("json parse error : ", e);
        }

        return result;
    }

    @Override
    public String buildErrorDataStorage() {
        return this.build();
    }

    protected InputParam getHostInput() {
        return getInputParam("host",
                "地址",
                "请填入连接地址", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入连接地址").build(),
                null);
    }

    protected InputParam getPortInput() {
        return getInputParam("port",
                "端口",
                "请填入端口号", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入端口号").build(),
                null);
    }

    protected InputParam getCatalogInput() {
        return null;
    }

    protected InputParam getSchemaInput() {
        return null;
    }

    protected InputParam getDatabaseInput() {
        return getInputParam("database",
                "数据库",
                "请填入数据库", 1, Validate.newBuilder().setRequired(true).setMessage("请填入数据库").build(),
                null);
    }

    protected InputParam getUserInput() {
        return getInputParam("user",
                "用户名",
                "请填入用户名", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入用户名").build(),
                null);
    }

    protected InputParam getPasswordInput() {
        return getInputParam("password",
                "密码",
                "请填入密码", 1,
                Validate.newBuilder().setRequired(false).setMessage("请填入密码").build(),
                null);
    }

    protected InputParam getPropertiesInput() {
        return getInputParam("properties",
                "参数",
                "请填入参数，格式为key=value&key1=value1", 2, null,
                null);
    }

    protected InputParam getInputParam(String field, String title, String placeholder, int rows, Validate validate , Object defaultValue) {
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

    protected List<PluginParams> getOtherParams() {

        return new ArrayList<>();
    }

}
