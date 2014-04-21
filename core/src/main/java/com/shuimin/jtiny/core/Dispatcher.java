package com.shuimin.jtiny.core;

import com.shuimin.jtiny.core.exception.HttpException;
import com.shuimin.jtiny.core.http.HttpMethod;
import com.shuimin.jtiny.core.misc.Attrs;
import com.shuimin.jtiny.core.misc.Config;
import com.shuimin.jtiny.core.misc.Makeable;
import com.shuimin.jtiny.core.mw.router.Router;

import static com.shuimin.jtiny.core.Server.G.debug;

/**
 * @author ed
 */
public class Dispatcher extends AbstractMiddleware
    implements Makeable<Dispatcher>, Attrs<Dispatcher> {

    final private Router router;

    public Dispatcher(Router r) {
        router = r;
    }

    public Dispatcher bind(int mask, String pattern, Middleware... mw) {
        router.add(mask, pattern, mw);
        return this;
    }

    public Dispatcher get(String pattern, Middleware... mw) {
        router.add(HttpMethod.mask(HttpMethod.GET), pattern, mw);
        return this;
    }

    public Dispatcher post(String pattern, Middleware... mw) {
        router.add(HttpMethod.mask(HttpMethod.POST), pattern, mw);
        return this;
    }

    public Dispatcher delete(String pattern, Middleware... mw) {
        router.add(HttpMethod.mask(HttpMethod.DELETE), pattern, mw);
        return this;
    }

    public Dispatcher put(String pattern, Middleware... mw) {
        router.add(HttpMethod.mask(HttpMethod.PUT), pattern, mw);
        return this;
    }


    public Router router() {
        return router;
    }

    public Dispatcher use(Config<Dispatcher> config) {
        config.config(this);
        return this;
    }


    @Override
    public ExecutionContext handle(ExecutionContext ctx) {
        Middleware processor = router.route(ctx);
        debug("found middleware" + processor);
        if (processor == null) throw new HttpException(404, ctx.req().path());
        return processor.exec(ctx);
    }
}
