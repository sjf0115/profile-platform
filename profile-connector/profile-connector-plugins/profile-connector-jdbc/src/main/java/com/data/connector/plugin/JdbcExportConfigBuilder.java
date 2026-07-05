package com.data.connector.plugin;

import com.data.connector.api.ExportConfigBuilder;
import com.data.profile.common.domain.connector.CommonConstants;
import com.data.profile.common.domain.connector.param.ParamsOptions;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.props.RadioParamProps;
import com.data.profile.common.domain.connector.param.props.SelectParamsProps;
import com.data.profile.common.domain.connector.param.type.RadioParam;
import com.data.profile.common.domain.connector.param.type.SelectParam;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * JDBC 投递配置表单构建
 * <p>MySQL/ClickHouse 共用，返回 table_name、write_mode、target_column 三个字段。</p>
 * <p>前端根据字段名约定自动加载选项（table_name → 表列表，target_column → 列列表）。</p>
 *
 * 作者：SmartSi
 */
@Slf4j
public class JdbcExportConfigBuilder implements ExportConfigBuilder {

    @Override
    public String build() {
        List<PluginParams> params = new ArrayList<>();
        params.add(getTableNameSelect());
        params.add(getWriteModeRadio());
        params.add(getTargetColumnSelect());

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

    private SelectParam getTableNameSelect() {
        return SelectParam.newBuilder("table_name", "目标表")
                .setProps(new SelectParamsProps().setPlaceholder("请选择目标数据表"))
                .setSize(CommonConstants.SMALL)
                .addValidate(Validate.newBuilder().setRequired(true).setMessage("请选择目标表").build())
                .build();
    }

    private RadioParam getWriteModeRadio() {
        return RadioParam.newBuilder("write_mode", "写入模式")
                .addParamsOptions(new ParamsOptions("追加", "append", false))
                .addParamsOptions(new ParamsOptions("覆盖", "upsert", false))
                .setValue("append")
                .setProps(new RadioParamProps())
                .build();
    }

    private SelectParam getTargetColumnSelect() {
        return SelectParam.newBuilder("target_column", "目标列")
                .setProps(new SelectParamsProps().setPlaceholder("请选择 upsert key 列"))
                .setSize(CommonConstants.SMALL)
                .build();
    }
}
