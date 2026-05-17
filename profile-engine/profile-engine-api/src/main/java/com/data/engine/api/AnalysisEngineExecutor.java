package com.data.engine.api;

import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.spi.SPI;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@SPI
public interface AnalysisEngineExecutor {

    // 初始化
    void init(ExecutorRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception;

    // 创建数据集引擎表
    void createTable(String tableName, 
                    List<Map<String, Object>> fields, 
                    String entityField,
                    String partitionField) throws Exception;

    // 修改数据集引擎表
    void alterTable(String tableName,
                   List<Map<String, Object>> addFields,
                   List<String> dropFields,
                   List<Map<String, Object>> modifyFields) throws Exception;

    // 删除数据集引擎表
    void dropTable(String tableName) throws Exception;

    // 同步数据到引擎表
    void syncData(ExecutorRequest syncRequest) throws Exception;

    // 圈选群组
    void selectedGroup() throws Exception;

    ProcessResult getProcessResult();

    ExecutorRequest getTaskRequest();
}
