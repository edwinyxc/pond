package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.common.abs.Attrs;
import com.shuimin.common.abs.Config;
import com.shuimin.common.abs.Makeable;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.exception.PondException;
import com.shuimin.pond.core.kernel.PKernel;
import com.shuimin.pond.core.spi.BaseServer;
import com.shuimin.pond.core.spi.ContextService;
import com.shuimin.pond.core.spi.Logger;
import com.shuimin.pond.core.spi.MiddlewareExecutor;

import java.io.File;
import java.util.LinkedList;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2014/5/8.
 */
public final class Pond implements Makeable<Pond>, Attrs<Pond> {

    private LinkedList<Middleware> mids = new LinkedList<>();
    private Logger logger;
    private BaseServer server;
    private ContextService service;
    private MiddlewareExecutor executor;
    private boolean init_flag = false;

    private Pond() {
    }

    public static Pond get() {
        if(!holder.instance.init_flag) {
            holder.instance._init();
        }
        return holder.instance;
    }

    public static Pond init(Config<Pond>... configs) {
        try {
            Pond pond = Pond.get();

            for (Config<Pond> conf : configs) {
                conf.config(pond);
            }

            return pond;
        } catch (PondException t) {
            throw new RuntimeException(t.toString(), t);
        }
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

    public static void debug(Object s) {
        Logger logger = Pond.get().logger;
        logger.debug(S.dump(s));
    }

    public static void config(String name, Object o) {
        get().attr(name, o);
    }

    @SuppressWarnings("unchecked")
    public static <E> E config(String name) {
        return (E) get().attr(name);
    }

    @SuppressWarnings("unchecked")
    public static void register(Class clazz, Object singleton) {
        PKernel.register(clazz, singleton);
    }

    public static <E> E register(Class clazz) {
        return PKernel.get(clazz.getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    public static <E> E attribute(String name) {
        return (E) Pond.get().attr(name);
    }

    @SuppressWarnings("unchecked")
    public static void attribute(String name, Object o) {
        Pond.get().attr(name, o);
    }

    /**
     * get absolute path relative to g.web_root
     *
     * @param path input relative path
     * @return absolute path
     */
    //TODO ugly name
    public static String pathRelWebRoot(String path) {
        String root = (String) Pond.get().attr(Global.ROOT);
        if (path == null) return null;
        if (S.path.isAbsolute(path)) {
            return path;
        }
        return root + File.separator + path;
    }

    /**
     * get absolute path relative to g.root
     *
     * @param path input relative path
     * @return absolute path
     */
    //TODO ugly name
    public static String pathRelRoot(String path) {
        String root = (String) Pond.get().attr(Global.ROOT_WEB);
        if (path == null) return null;
        if (S.path.isAbsolute(path)) {
            return path;
        }
        return root + File.separator + path;
    }

    public Pond webRoot(String relPath) {
        this.attr(Global.ROOT_WEB, this.attr(Global.ROOT)
                + File.separator + relPath);
        return this;
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
        Logger.allowDebug(true);
        return this;
    }

    public Pond use(Middleware... mids) {
        try {
            this.mids.addAll(_for(mids)
                    .each(Middleware::init).toList());
        } catch (PondException pe) {
            throw new RuntimeException(pe.toString(), pe);
        } catch (Exception e) {
            throw e;
        }
        return this;
    }

    private Pond _init() {
        String rootPath = S.path.rootClassPath();
        //do not change this
        this.attr(Global.ROOT, rootPath);
        //default
        this.attr(Global.ROOT_WEB, rootPath
                + File.separator + "www");

//        this.attr(Global.ROOT, S.path.webRoot());

        logger = PKernel.getLogger();

        logger.info("root : " + rootPath);

        server = find(BaseServer.class);

        service = find(ContextService.class);

        executor = find(MiddlewareExecutor.class);

        logger.info("Installing Handler");
        server.installHandler(handler(service, executor));
        logger.info("... Finished");

        this.init_flag = true;

        return this;
    }

    private <E> E find(Class<E> s) {
        E e = PKernel.getService(s);
        if (e == null) throw new NullPointerException(s.getSimpleName() + "not found");
        logger.info("Get " + s.getSimpleName()+": " + e.getClass().getCanonicalName());
        return e;
    }

    private Callback.C2<Request, Response> handler(
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

    @Override
    public Pond attr(String name, Object o) {
        PKernel.register(name, o, null);
        return this;
    }

    @Override
    public Object attr(String name) {
        return PKernel.get(name);
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

    private static class holder {
        final static Pond instance = new Pond();
    }

}
