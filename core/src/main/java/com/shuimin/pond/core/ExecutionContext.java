package com.shuimin.pond.core;

import com.shuimin.common.abs.Attrs;
import com.shuimin.pond.core.http.Request;
import com.shuimin.pond.core.http.Response;

/**
 * @author ed
 */
public interface ExecutionContext extends Attrs<ExecutionContext> {

    public Request req();

    public Response resp();

    /**
     * last result holder
     *
     * @return return value of last execution
     */
    public Object last();

    //TODO :加入type-safe-insurance

    static ExecutionContext init(Request req, Response resp) {
        return new ExecutionContext() {

            @Override
            public Request req() {
                return req;
            }

            @Override
            public Response resp() {
                return resp;
            }

            @Override
            public Object last() {
                return null;
            }
        };
    }

    public default ExecutionContext next(Object value) {
        ExecutionContext _this = this;
        return new ExecutionContext() {

            @Override
            public Request req() {
                return _this.req();
            }

            @Override
            public Response resp() {
                return _this.resp();
            }

            @Override
            public Object last() {
                return value;
            }

        };
    }

    public static ExecutionContext CUR() {
        return ExecutionManager.ExecutionContexts.get();
    }

    public static Request REQ() {
        return CUR().req();
    }

    public static Response RESP() {
        return CUR().resp();
    }

}

