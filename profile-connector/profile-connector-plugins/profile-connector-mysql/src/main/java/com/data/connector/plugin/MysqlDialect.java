package com.data.connector.plugin;

public class MysqlDialect extends JdbcDialect {

    @Override
    public String getDriver() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public boolean invalidateItemCanOutputToSelf() {
        return true;
    }

    @Override
    public boolean supportToBeErrorDataStorage() {
        return true;
    }

    @Override
    public String quoteIdentifier(String entity) {
        return "`" + entity + "`";
    }

    @Override
    public String getQuoteIdentifier() {
        return "`";
    }
}
