package com.data.connector.plugin;

import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.type.InputParam;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MysqlConfigBuilder extends JdbcConfigBuilder {

    @Override
    public String buildErrorDataStorage() {
        List<PluginParams> params = new ArrayList<>();
        params.add(getHostInput());
        params.add(getPortInput());
        if (getCatalogInput() != null) {
            params.add(getCatalogInput());
        }

        params.add(getErrorDataStorageDatabaseInput());

        if (getSchemaInput() != null) {
            params.add(getSchemaInput());
        }

        params.add(getUserInput());
        params.add(getPasswordInput());
        params.add(getPropertiesInput());

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
    protected InputParam getPropertiesInput() {
        return getInputParam("properties",
                "参数",
                "请填入参数，格式为key=value&key1=value1", 2, null,
                "useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&useInformationSchema=true&allowPublicKeyRetrieval=true");
    }

    @Override
    protected InputParam getDatabaseInput() {
        return getInputParam("database",
                "数据库",
                "请填入数据库", 1, null,
                null);
    }

    @Override
    protected InputParam getPortInput() {
        return getInputParam("port",
                "端口",
                "请填入端口号", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入端口号").build(),
                3306);
    }

    protected InputParam getErrorDataStorageDatabaseInput() {
        return getInputParam("database",
                "数据库",
                "请填入数据库", 1,
                Validate.newBuilder().setRequired(true).setMessage("请填入数据库").build(),
                null);
    }
}
