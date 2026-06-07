package com.data.engine.api;

import java.util.List;

@Deprecated
public interface Execution<SR extends Component, TF extends Component, SK extends Component> {

    void execute(List<SR> sources, List<TF> transforms, List<SK> sinks) throws Exception;

    void stop() throws Exception;

    void prepare() throws Exception;
}
