package com.data.connector.api;

import java.util.List;

public interface StatementSplitter {

    List<String> splitStatements(String body);
}
