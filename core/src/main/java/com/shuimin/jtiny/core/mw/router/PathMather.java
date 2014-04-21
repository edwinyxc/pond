package com.shuimin.jtiny.core.mw.router;

import com.shuimin.jtiny.core.http.Request;

/**
 * Created by ed interrupt 2014/4/2.
 */
public interface PathMather {

    public boolean match(Request req);

}
