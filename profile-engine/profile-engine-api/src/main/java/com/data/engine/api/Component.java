package com.data.engine.api;

import com.data.spi.SPI;

@Deprecated
@SPI
public interface Component extends Plugin {
    void prepare(RuntimeEnvironment env) throws Exception;
}
