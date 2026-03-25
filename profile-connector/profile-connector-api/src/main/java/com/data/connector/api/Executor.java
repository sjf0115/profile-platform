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
}
