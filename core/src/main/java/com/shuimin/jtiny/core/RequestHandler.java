package com.shuimin.jtiny.core;

import com.shuimin.jtiny.core.http.Response;
import com.shuimin.jtiny.core.http.Request;

/**
 * @author ed
 */
public interface RequestHandler {

    public void handle(Request req, Response resp);

}
