package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.common.abs.Attrs;
import com.shuimin.common.abs.Config;
import com.shuimin.common.abs.Makeable;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.kernel.PKernel;
import com.shuimin.pond.core.spi.*;

import java.util.LinkedList;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2014/5/8.
 */
public final class Pond implements Makeable<Pond>, Attrs<Pond> {

    private static class holder {
        final static Pond instance = new Pond();
    }

    public static Pond get() {
        return holder.instance;
    }

    private LinkedList<Middleware> mids = new LinkedList<>();

    private Logger logger;
    private BaseServer server;
    private ContextService service;
    private MiddlewareExecutor executor;
    private ViewEngine viewEngine;

    public static Pond init(Config<Pond>... configs) {
        Pond pond = Pond.get();
        pond.init();
        for (Config<Pond> conf : configs) {
            conf.config(pond);
        }
        return pond;
    }

    private Pond() {
    }

    public void start(int port) {
        logger.info("Starting server...");
        server.listen(port);
        logger.info("Server binding port: " + port);
    }

    public void stop() {
        logger.info("Stopping server...");
        server.stop();
        logger.info("... finished");
    }

    public Pond debug() {
        logger.allowDebug(true);
        return this;
    }


    public Pond use(Middleware... mids) {
        this.mids.addAll(_for(mids)
            .each(Middleware::init).toList());
        return this;
    }

    private void init() {

        this.attr(Global.ROOT,S.path.webRoot());


        logger = PKernel.getLogger();

        logger.info("web root : "+ this.attr(Global.ROOT));

        server = find(BaseServer.class);

        service = find(ContextService.class);

        executor = find(MiddlewareExecutor.class);

        viewEngine = find(ViewEngine.class);

        logger.info("Installing Handler");
        server.installHandler(handler(service, executor));
        logger.info("... Finished");

    }

    private <E> E find(Class<E> s) {
        logger.info("Getting " + s.getSimpleName());
        E e = PKernel.getService(s);
        if (e == null) throw new NullPointerException(s.getSimpleName() + "not found");
        return e;
    }

    private Callback._2<Request, Response> handler(
        ContextService service,
        MiddlewareExecutor executor
    ) {
        return (req, resp) -> {
            ExecutionContext ctx = ExecutionContext.init(req, resp);
            /*install to thread-local*/
            service.set(ctx);
            executor.execute(() -> ctx, () -> this.mids);
            /*remove from thread*/
            service.remove(ctx);
        };
    }

    public static ExecutionContext CUR() {
        return Pond.get().service.get();
    }

    public static Request REQ() {
        return CUR().req();
    }

    public static Response RESP() {
        return CUR().resp();
    }

    public static void debug(String s) {
        Pond.get().logger.debug(S.dump(s));
    }

    public static void config(String name, Object o) {
        get().attr(name, o);
    }

    public static <E> E config(String name) {
        return (E) get().attr(name);
    }

    @Override
    public Pond attr(String name, Object o) {
        PKernel.register(name, o, null);
        return this;
    }

    @Override
    public Object attr(String name) {
        return PKernel.get(name);
    }

    public static void register(Class clazz, Object singleton) {
        PKernel.register(clazz, singleton);
    }

    public static <E> E register(Class clazz) {
        return PKernel.get(clazz.getCanonicalName());
    }

    @Override
    public String toString() {
        return "Pond{" +
            "mids=" + mids +
            ", logger=" + logger +
            ", server=" + server +
            ", service=" + service +
            ", executor=" + executor +
            '}';
    }


}