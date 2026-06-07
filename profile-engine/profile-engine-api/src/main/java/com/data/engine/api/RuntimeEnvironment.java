package com.data.engine.api;

import com.data.spi.SPI;

@Deprecated
@SPI
public interface RuntimeEnvironment extends Plugin {

    void prepare();

    Execution getExecution();
}
