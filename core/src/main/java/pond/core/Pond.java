package pond.core;

import pond.common.S;
import pond.common.spi.JsonService;
import pond.common.spi.SPILoader;
import pond.core.exception.PondException;
import pond.core.session.SessionManager;
import pond.core.spi.*;
import pond.core.session.SessionInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

import static pond.common.S.*;

/**
 * Main Class
 */
public final class Pond implements RouterAPI {

    static Logger logger = LoggerFactory.getLogger(Pond.class);

    public final static String DEFAULT_DB = "_db";
    public final static String MULTIPART_RESOLVER = "_multipart_resolver";
    private BaseServer server;
    private Router rootRouter;
    public final Config config = new Config();
    public final Map<String, Object> container = new HashMap<>();

    public Object component(String k) {
        return container.get(k);
    }

    DefaultStaticFileServer staticFileServer;

    public Pond component(String k, Object v) {
        container.put(k, v);
        return this;
    }

    //Before the routing chain
    final List<Mid> before = new LinkedList<>();

    //Session Manager (could be null)
    SessionManager sessionManager;

    // Executor
    final CtxExec ctxExec = new CtxExec();

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
        String root = S.path.rootClassPath();
        String webroot = S.path.detectWebRootPath();

        //map properties
        File configFile = new File(root + File.separator + Config.CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.canRead()) {
            loadConfig(S.file.loadProperties(configFile));
        } else {
            logger.info("config file not exists. using default values");
            //TODO
        }

        //do not change these
        //FIXME ugly
        config.put(Config.ROOT, root);
        config.put(Config.ROOT_WEB, webroot);

        config.put(Config.WWW_PATH, webroot + File.separator + avoidNull(config.get(Config.WWW_NAME), "www"));
        config.put(Config.VIEWS_PATH, webroot + File.separator + avoidNull(config.get(Config.VIEWS_NAME), "views"));

//      this.attr(Global.ROOT, S.path.webRoot()#);

        logger.info("root : " + root);
        server = spi(BaseServer.class);
        server.pond(this);

        //TODO ADD CONFIG
        //TODO CHANGE CONFIG LAYER
        //router
        rootRouter = new Router();

        //engine
        ViewEngine vg = spi(ViewEngine.class);
        try {
            vg.configViewPath(config.get(Config.VIEWS_PATH));
        } catch (Exception e) {
            debug(e.getMessage());
        }

        viewEngines.put("default", vg);
        logger.info("Installing Handler");
        //init handler
        logger.info("... Finished");

    }

    public Pond listen(int port){
        System.setProperty(BaseServer.PORT, String.valueOf(port));
        listen();
        return this;
    }

    public Pond listen() {

        logger.info("Starting server...");
        //append dispatcher to the chain
        LinkedList<Mid> mids = new LinkedList<>(before);
        mids.add(rootRouter);

        //TODO CONFIG LAYER
        server.executor(Executors.newFixedThreadPool(8));

        server.handler((req, resp) -> {
            Ctx ctx = new Ctx(req, resp, this, mids);
            ctxExec.exec(ctx);
        });

        try {
            server.listen();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(S.dump(e.getStackTrace()));
        }

        return this;
    }

    public Mid _static(String dir) {
        if (staticFileServer == null) {
            staticFileServer = new DefaultStaticFileServer(dir);
        }
        return staticFileServer;
    }

    public Map<String, Session> sessions() {
        _assert(sessionManager, "Please use session first");
        return sessionManager.getAll();
    }

    /**
     * Returns a MW that handle session
     * see more at com.shuimin.pond.core.mw.session.Session
     */
    public SessionInstaller useSession() {
        if (this.sessionManager == null) {
            this.sessionManager = new SessionManager(this);
        }
        return new SessionInstaller(this.sessionManager);
    }

    /**
     * get Session
     */
    Session session(Ctx ctx) {
        if (this.sessionManager == null) {
            throw new RuntimeException("Please use Pond.before(pond.useSession()) first");
        }
        return this.sessionManager.get(ctx);
    }


    /**
     * Returns JsonService
     */
    public JsonService json() {
        return SPILoader.service(JsonService.class);
    }

    public Pond loadConfigFromCmdLine(String[] args) {
        this.config.readFromFile(args);
        return this;
    }

    /**
     * Load attributes from properties
     */
    public Pond loadConfig(Properties conf) {
        config.load(conf);
        return this;
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
            Pond pond = new Pond();

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
        S._debug(logger, log ->
                log.debug(S.dump(s)));
    }


    /**
     * get absolute path relative to g.web_root
     *
     * @param path input relative path
     * @return absolute path
     */
    public String pathRelWebRoot(String path) {
        String root = config.get(Config.ROOT_WEB);
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
        String root = config.get(Config.ROOT);
        if (path == null) return null;
        if (S.path.isAbsolute(path)) {
            return path;
        }
        return root + File.separator + path;
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
     */
    private Pond _init() {
        return this;
    }

    public <E> E spi(Class<E> s) {
        E e = SPILoader.service(s);
        if (e == null)
            throw new NullPointerException(s.getSimpleName() + "not found");
        else if (e instanceof EnvSPI) {
            ((EnvSPI) e).env(this.config);
        }
        logger.info("SPI-INJECT" +
                s.getSimpleName() + ": " + e.getClass().getCanonicalName());
        return e;
    }


    @Deprecated
    public Object attr(String s) {
        return this.config.get(s);
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
