package pond.core;

import pond.common.S;
import pond.common.SPILoader;
import pond.common.abs.Attrs;
import pond.core.exception.PondException;
import pond.core.router.Router;
import pond.core.spi.BaseServer;
import pond.core.spi.ViewEngine;
import pond.core.spi.JsonService;
import pond.core.session.SessionInstaller;
import pond.core.spi.MultipartRequestResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Class
 */
public final class Pond implements  RouterAPI {

    static Logger logger = LoggerFactory.getLogger(Pond.class);
    private BaseServer server;
    private Router rootRouter;
    //config
    private Map attrs = new HashMap();

    //Before the routing chain
    private List<Mid> before = new LinkedList<>();

    //After the routing chain

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

    public Pond _static(String dir) {
        server.installStatic(server.staticFileServer(dir));
        return this;
    }

    /**
     * Returns a MW that handle session
     * see more at com.shuimin.pond.core.mw.session.Session
     */
    public SessionInstaller _session() {
        return new SessionInstaller();
    }

    public <E> E spi( Class<E> spiClass ) {
        E service = SPILoader.service( spiClass );
        if( service instanceof PondSPI) {
            ((PondSPI) service).pond(this);
        }
        else if( service instanceof EnvSPI ) {
            ((EnvSPI) service).env(this.config);
        }
        return service;
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
    public static Pond init(pond.common.abs.Config<Pond>... configs) {
        try {
            Pond pond = new Pond()._init();

            for (pond.common.abs.Config<Pond> conf : configs) {
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



    /**
     * get absolute path relative to g.web_root
     *
     * @param path input relative path
     * @return absolute path
     */
    public String pathRelWebRoot(String path) {
        String root = (String) attr(Config.ROOT_WEB);
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
    public String pathRelRoot(String path) {
        String root = (String) attr(Config.ROOT);
        if (path == null) return null;
        if (S.path.isAbsolute(path)) {
            return path;
        }
        return root + File.separator + path;
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
//        DefaultFileServer fileServer = new DefaultFileServer("www");
//        mids.add(new DefaultFileServer("www"));
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
        
        //map properties
        Properties conf = Config.loadProperties(Config.CONFIG_FILE_NAME);
        //TODO

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
        if (e == null)
            throw new NullPointerException(s.getSimpleName() + "not found");
        logger.info("Get " + 
                s.getSimpleName() + ": " + e.getClass().getCanonicalName());
        return e;
    }

    public Pond attr(String name, Object o) {
        this.attrs.put( name, o );
        return this;
    }

    public Object attr(String name) {
        return this.attrs.get( name );
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


}
