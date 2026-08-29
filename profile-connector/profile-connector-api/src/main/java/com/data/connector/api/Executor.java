package com.data.connector.api;

import com.data.profile.common.domain.connector.request.ConnectorResponse;
import com.data.profile.common.domain.connector.request.ExecuteRequestParam;

public interface Executor {

    default ConnectorResponse queryForPage(ExecuteRequestParam param) throws Exception {
        return null;
    }

    default ConnectorResponse queryForList(ExecuteRequestParam param) throws Exception {
        return null;
    }

    default ConnectorResponse queryForOne(ExecuteRequestParam param) throws Exception {
        return null;
    }

    /**
     * execute script
     * @param param param
     * @return Map<String,Object>
     */
    default ConnectorResponse deleteData(ExecuteRequestParam param) throws Exception {
        return null;
    }

    /**
     * 批量写入数据行（投递/数据管道场景）。
     *
     * <p>使用 param.tableName + param.rows；列顺序取首行 keySet，参数化写入防注入。</p>
     *
     * @param param 含 tableName 与 rows 的执行参数
     * @return result 为写入行数（Long）
     */
    default ConnectorResponse insertData(ExecuteRequestParam param) throws Exception {
        return null;
    }
}
