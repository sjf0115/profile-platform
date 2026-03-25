package com.data.connector.api;

public interface ConfigBuilder {

    String build(boolean isEn);

    String buildErrorDataStorage(boolean isEn);
}
