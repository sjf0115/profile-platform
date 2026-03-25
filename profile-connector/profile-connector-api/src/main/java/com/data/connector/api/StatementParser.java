package com.data.connector.api;

import com.data.connector.api.entity.StatementMetadataFragment;

public interface StatementParser {

    StatementMetadataFragment parseStatement(String statement);
}
