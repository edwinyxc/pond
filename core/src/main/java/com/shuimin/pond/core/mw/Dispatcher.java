package com.shuimin.pond.core.mw;

import com.shuimin.common.abs.Config;
import com.shuimin.common.abs.Makeable;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.Response;
import com.shuimin.pond.core.exception.HttpException;
import com.shuimin.pond.core.http.HttpMethod;
import com.shuimin.pond.core.kernel.PKernel;
import com.shuimin.pond.core.spi.Router;

import static com.shuimin.pond.core.Pond.debug;


/**
 * @author ed
 */
public class Dispatcher extends AbstractMiddleware
        implements Makeable<Dispatcher> {

    final private Router router = PKernel.getService(Router.class);


    public Dispatcher bind(int mask, String pattern, Middleware... mw) {
        router.add(mask, pattern, mw);
        return this;
    }

    public Dispatcher get(String pattern, Callback<?> cb) {
        return get(pattern, Action.consume(cb));
    }

    public Dispatcher get(String pattern, Callback.C2<Request, Response> cb) {
        return get(pattern, Action.simple(cb));
    }

    public Dispatcher post(String pattern, Callback<?> cb) {
        return post(pattern, Action.consume(cb));
    }

    public Dispatcher post(String pattern, Callback.C2<Request, Response> cb) {
        return post(pattern, Action.simple(cb));
    }

    public Dispatcher delete(String pattern, Callback<?> cb) {
        return delete(pattern, Action.consume(cb));
    }

    public Dispatcher delete(String pattern, Callback.C2<Request, Response> cb) {
        return delete(pattern, Action.simple(cb));
    }

    public Dispatcher put(String pattern, Callback<?> cb) {
        return put(pattern, Action.consume(cb));
    }

    public Dispatcher put(String pattern, Callback.C2<Request, Response> cb) {
        return put(pattern, Action.simple(cb));
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
