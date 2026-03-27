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
    public String buildErrorDataStorage(boolean isEn) {
        List<PluginParams> params = new ArrayList<>();
        params.add(getHostInput(isEn));
        params.add(getPortInput(isEn));
        if (getCatalogInput(isEn) != null) {
            params.add(getCatalogInput(isEn));
        }

        params.add(getErrorDataStorageDatabaseInput(isEn));

        if (getSchemaInput(isEn) != null) {
            params.add(getSchemaInput(isEn));
        }

        params.add(getUserInput(isEn));
        params.add(getPasswordInput(isEn));
        params.add(getPropertiesInput(isEn));

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
    protected InputParam getPropertiesInput(boolean isEn) {
        return getInputParam("properties",
                isEn ? "properties" : "参数",
                isEn ? "please enter properties,like key=value&key1=value1" : "请填入参数，格式为key=value&key1=value1", 2, null,
                "useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&useInformationSchema=true&allowPublicKeyRetrieval=true");
    }

    @Override
    protected InputParam getDatabaseInput(boolean isEn) {
        return getInputParam("database",
                isEn ? "database" : "数据库",
                isEn ? "please enter database" : "请填入数据库", 1, null,
                null);
    }

    @Override
    protected InputParam getPortInput(boolean isEn) {
        return getInputParam("port",
                isEn ? "port" : "端口",
                isEn ? "please enter port" : "请填入端口号", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter port" : "请填入端口号").build(),
                3306);
    }

    protected InputParam getErrorDataStorageDatabaseInput(boolean isEn) {
        return getInputParam("database",
                isEn ? "database" : "数据库",
                isEn ? "please enter database" : "请填入数据库", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter database" : "请填入数据库").build(),
                null);
    }
}
