package com.data.connector.plugin;

import com.data.connector.api.ExportConfigBuilder;
import com.data.profile.common.domain.connector.CommonConstants;
import com.data.profile.common.domain.connector.param.ParamsOptions;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.props.SelectParamsProps;
import com.data.profile.common.domain.connector.param.type.SelectParam;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * MinIO 投递配置表单构建
 * <p>返回 object_path 字段，bucket 已在数据源配置中，格式固定 CSV。</p>
 * <p>object_path 存储模板字符串，执行时替换 {groupId}、{timestamp} 等变量。</p>
 *
 * 作者：SmartSi
 */
@Slf4j
public class MinioExportConfigBuilder implements ExportConfigBuilder {

    @Override
    public String build() {
        List<PluginParams> params = new ArrayList<>();
        params.add(getObjectPathSelect());

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

    private SelectParam getObjectPathSelect() {
        return SelectParam.newBuilder("object_path", "对象路径")
                .addParamsOptions(new ParamsOptions("按 group 分目录（带时间戳）", "/groups/{groupId}/{timestamp}.csv", false))
                .addParamsOptions(new ParamsOptions("按时间戳分目录", "{timestamp}/groups/{groupId}.csv", false))
                .addParamsOptions(new ParamsOptions("简单路径（无时间戳）", "/groups/{groupId}.csv", false))
                .setValue("/groups/{groupId}/{timestamp}.csv")
                .setProps(new SelectParamsProps().setPlaceholder("请选择对象路径模板"))
                .setSize(CommonConstants.SMALL)
                .addValidate(Validate.newBuilder().setRequired(true).setMessage("请选择对象路径模板").build())
                .build();
    }
}
