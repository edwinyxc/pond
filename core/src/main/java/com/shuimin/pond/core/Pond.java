package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.common.SPILoader;
import com.shuimin.common.abs.Attrs;
import com.shuimin.pond.core.exception.PondException;
import com.shuimin.pond.core.mw.StaticFileServer;
import com.shuimin.pond.core.router.Router;
import com.shuimin.pond.core.spi.BaseServer;
import com.shuimin.pond.core.spi.ViewEngine;
import com.shuimin.pond.core.spi.JsonService;
import com.shuimin.pond.core.spi.MultipartRequestResolver;
import com.shuimin.pond.core.mw.session.SessionInstaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Main Class
 */
public final class Pond implements Attrs<Pond>, RouterAPI {

    static Logger logger = LoggerFactory.getLogger(Pond.class);
    private BaseServer server;
    private Router rootRouter;
    private List<Mid> before = new LinkedList<>();
//    private List<Mid> after = new LinkedList<>();

    private ConcurrentMap<String, Object> holder =
            new ConcurrentHashMap<>();

    private Map<String, ViewEngine> viewEngines
            = new HashMap<String, ViewEngine>() {{
        //do not put any code here
    }};

    public Pond before(Mid mid) {
        before.add(mid);
        return this;
    }

//    public Pond after(Mid mid) {
//        after.add(mid);
//        return this;
//    }

    private Pond() {
    }

    //static method 
    //
    //

    
    /**
     * Returns MultipartResolver
     */
    public static MultipartRequestResolver multipart() {
        return SPILoader.service(MultipartRequestResolver.class);
    }

    
    /**
     * 
     * Returns JsonService
     */
    public static JsonService json() {
        return SPILoader.service(JsonService.class);
    }

    /**
     * Custom initialization
     *
     * @param configs
     * @return
     */
    @SafeVarargs
    public static Pond init(com.shuimin.common.abs.Config<Pond>... configs) {
        try {
            Pond pond = Pond.get();

            for (com.shuimin.common.abs.Config<Pond> conf : configs) {
                conf.config(pond);
            }

            return pond;
        } catch (PondException t) {
            throw new RuntimeException(t.toString(), t);
        }
    }

    public static String _ignoreLastSlash(String path) {
        if (!"/".equals(path) && path.endsWith("/"))
            return path.substring(0, path.length() - 1);
        return path;
    }

    public static void debug(Object s) {
        Logger logger = Pond.logger;
        logger.debug(S.dump(s));
    }

    public static void config(String name, Object o) {
        get().attr(name, o);
    }

    @SuppressWarnings("unchecked")
    public static <E> E config(String name) {
        return (E) get().attr(name);
    }

    /**
     * get absolute path relative to g.web_root
     *
     * @param path input relative path
     * @return absolute path
     */
    //TODO ugly name
    public static String pathRelWebRoot(String path) {
        String root = (String) Pond.get().attr(Config.ROOT_WEB);
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
        String root = (String) Pond.get().attr(Config.ROOT);
        if (path == null) return null;
        if (S.path.isAbsolute(path)) {
            return path;
        }
        return root + File.separator + path;
    }
    
    /**
     * Returns a MW that handle session
     * see more at com.shuimin.pond.core.mw.session.Session
     */

    public SessionInstaller _session() {
        return new SessionInstaller();
    }
    
    /**
     * Returns a new MW that handle static files
     *
     */
    public StaticFileServer _static(String dir) {
        return new StaticFileServer(dir);
    }

    /**
     * Get current Pond instance, or create one if not exist.
     */
    public static Pond get() {
        return Holder.instance;
    }

    /**
     * Enable setting/function/feature
     */
    public Pond enable(String setting) {
        set(setting, true);
        return this;
    }

    /**
     * Disable setting/function/feature
     */
    public Pond disable(String setting) {
        set(setting, false);
        return this;
    }

    /**
     * Set global settings
     */
    public Pond set(String attr, Object val) {
        return attr(attr, val);
    }

    /**
     * Get global settings
     */
    public Object get(String attr) {
        return attr(attr);
    }

    public ViewEngine viewEngine(String ext) {
        ViewEngine ret = viewEngines.get(ext);
        if (ret == null) ret = viewEngines.get("default");
        return ret;
    }

    public Pond viewEngine(String ext, ViewEngine viewEngine) {
        viewEngines.put(ext, viewEngine);
        return this;
    }


    public void listen(int port) {
        logger.info("Starting server...");
        //append dispatcher to the chain
        List<Mid> mids = new LinkedList<>(before);
        mids.add(rootRouter);
        //TODO test
//        mids.addAll(after);
//        StaticFileServer fileServer = new StaticFileServer("www");
//        mids.add(new StaticFileServer("www"));
        server.installHandler((req, resp) ->
                CtxExec.exec(new Ctx(req, resp, mids),
                        Collections.emptyList()));
//                        S.list(fileServer)));
        server.listen(port);
        logger.info("Server binding port: " + port);
    }

    public void stop() {
        logger.info("Stopping server...");
        server.stop();
        logger.info("... finished");
    }
    
    /**
     * DO NOT USE 
     * MAY BE DELETED IN FUTURE
     */
    @Deprecated
    public Pond debug() {
        //FIXME
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        return this;
    }


    /**
     * default initialization
     *
     * @return
     */
    private Pond _init() {
        String root = S.path.rootClassPath();
        String webroot = S.path.detectWebRootPath();
        //do not change these
        this.attr(Config.ROOT, root);
        this.attr(Config.ROOT_WEB, webroot);

        this.attr(Config.WWW_PATH, webroot + File.separator + "www");
        this.attr(Config.VIEWS_PATH, webroot + File.separator + "views");
//        this.attr(Global.ROOT, S.path.webRoot()#);

        logger.info("root : " + root);
        server = find(BaseServer.class);
        //TODO ADD CONFIG
        //router
        rootRouter = new Router();
        //engine
        ViewEngine vg = find(ViewEngine.class);
        try {
            vg.configViewPath((String) this.attr(Config.VIEWS_PATH));
        } catch (Exception e) {
            debug(e.getMessage());
        }
        viewEngines.put("default", vg);
        logger.info("Installing Handler");
        //init handler
        logger.info("... Finished");


        return this;
    }

    private <E> E find(Class<E> s) {
        E e = SPILoader.service(s);
        if (e == null) throw new NullPointerException(s.getSimpleName() + "not found");
        logger.info("Get " + s.getSimpleName() + ": " + e.getClass().getCanonicalName());
        return e;
    }

    @Override
    public Pond attr(String name, Object o) {
        holder.put(name, o);
        return this;
    }

    @Override
    public Object attr(String name) {
        return holder.get(name);
    }

    @Override
    public Map<String, Object> attrs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RouterAPI use(int mask, String path, Mid... mids) {
        return this.rootRouter.use(mask, path, mids);
    }

    @Override
    public RouterAPI use(String path, Router router) {
        return this.rootRouter.use(path, router);
    }

    /**
     * singleton
     */
    private static class Holder {
        final static Pond instance = new Pond()._init();
    }

}
