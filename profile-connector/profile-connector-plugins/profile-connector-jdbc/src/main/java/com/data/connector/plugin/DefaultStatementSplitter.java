
package com.data.connector.plugin;

import com.data.connector.api.StatementSplitter;
import com.data.profile.common.utils.StringUtils;

import java.util.Arrays;
import java.util.List;

public class DefaultStatementSplitter implements StatementSplitter {

    @Override
    public List<String> splitStatements(String body) {
        return StringUtils.isEmpty(body) ? null : Arrays.asList(body.split(";"));
    }
}
