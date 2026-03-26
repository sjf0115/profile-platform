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
    public String build(boolean isEn) {
        List<PluginParams> params = new ArrayList<>();
        params.add(getHostInput(isEn));
        params.add(getPortInput(isEn));
        if (getCatalogInput(isEn) != null) {
            params.add(getCatalogInput(isEn));
        }

        params.add(getDatabaseInput(isEn));

        if (getSchemaInput(isEn) != null) {
            params.add(getSchemaInput(isEn));
        }

        params.add(getUserInput(isEn));
        params.add(getPasswordInput(isEn));
        params.add(getPropertiesInput(isEn));

        params.addAll(getOtherParams(isEn));

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
    public String buildErrorDataStorage(boolean isEn) {
        return this.build(isEn);
    }

    protected InputParam getHostInput(boolean isEn) {
        return getInputParam("host",
                isEn ? "host":"地址",
                isEn ? "please enter host ip" : "请填入连接地址", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter host ip" : "请填入连接地址").build(),
                null);
    }

    protected InputParam getPortInput(boolean isEn) {
        return getInputParam("port",
                isEn ? "port" : "端口",
                isEn ? "please enter port" : "请填入端口号", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter port" : "请填入端口号").build(),
                null);
    }

    protected InputParam getCatalogInput(boolean isEn) {
        return null;
    }

    protected InputParam getSchemaInput(boolean isEn) {
        return null;
    }

    protected InputParam getDatabaseInput(boolean isEn) {
        return getInputParam("database",
                isEn ? "database" : "数据库",
                isEn ? "please enter database" : "请填入数据库", 1, Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter database" : "请填入数据库").build(),
                null);
    }

    protected InputParam getUserInput(boolean isEn) {
        return getInputParam("user",
                isEn ? "user" : "用户名",
                isEn ? "please enter user" : "请填入用户名", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter user" : "请填入用户名").build(),
                null);
    }

    protected InputParam getPasswordInput(boolean isEn) {
        return getInputParam("password",
                isEn ? "password" : "密码",
                isEn ? "please enter password" : "请填入密码", 1,
                Validate.newBuilder().setRequired(false).setMessage(isEn ? "please enter password" : "请填入密码").build(),
                null);
    }

    protected InputParam getPropertiesInput(boolean isEn) {
        return getInputParam("properties",
                isEn ? "properties" : "参数",
                isEn ? "please enter properties,like key=value&key1=value1" : "请填入参数，格式为key=value&key1=value1", 2, null,
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

    protected List<PluginParams> getOtherParams(boolean isEn) {

        return new ArrayList<>();
    }

}
