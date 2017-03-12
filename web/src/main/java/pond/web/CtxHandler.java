package pond.web;

import pond.common.S;
import pond.common.f.Callback;

/**
 * Created by ed on 20/02/17.
 */
public interface CtxHandler extends Callback<Ctx> {

    CtxHandler NOOP = ctx -> {
    };

    CtxHandler[] EMPTY_ARRAY = new CtxHandler[0];


    static CtxHandler express(Mid m) {
        return ctx -> {
            if (ctx instanceof HttpCtx) {
                HttpCtx hctx = (HttpCtx) ctx;
                m.apply(hctx.req, hctx.resp);
            } else {
                throw new RuntimeException("can't Convert a non-http-web-ctx-handler to a middleware");
            }
        };
    }


}
