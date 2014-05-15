package com.shuimin.pond.core.spi;

import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.Response;

/**
 * Created by ed on 2014/5/8.
 */
public interface BaseServer {

    public void listen(int port);

    public void stop();

    public void installHandler(Callback._2<Request,Response> handler);
}
