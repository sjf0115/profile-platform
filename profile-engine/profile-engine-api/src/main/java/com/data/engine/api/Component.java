package com.data.engine.api;

import com.data.spi.SPI;

@SPI
public interface Component extends Plugin {
    void prepare(RuntimeEnvironment env) throws Exception;
}
