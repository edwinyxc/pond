package pond.core;

import pond.common.EnvSPI;
import pond.common.S;
import pond.common.SPILoader;
import pond.common.spi.JsonService;
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

import static pond.common.S.*;

/**
 * Main Class
 */
public final class Pond implements  RouterAPI {

    public final static String DEFAULT_DB = "_db";

    static Logger logger = LoggerFactory.getLogger(Pond.class);
    private BaseServer server;
    private Router rootRouter;
    public final Config config = new Config();

    //Before the routing chain
    final List<Mid> before = new LinkedList<>();

    //Session Manager (could be null)
    SessionManager sessionManager;

    // Executor
    final CtxExec executor = new CtxExec();

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
        File configFile = new File( root + File.separator + Config.CONFIG_FILE_NAME);

        if ( configFile.exists() && configFile.canRead()) {
            loadConfig(S.file.loadProperties( configFile ));
        } else {
            logger.info("config file not exists. using default values");
            //TODO
        }

        //do not change these
        config.put(Config.ROOT, root);
        config.put(Config.ROOT_WEB, webroot);

        config.put(Config.WWW_PATH, webroot
                + File.separator + _notNullElse(
                config.get(Config.WWW_NAME), "www"));
        config.put(Config.VIEWS_PATH, webroot
                + File.separator + _notNullElse(
                config.get(Config.VIEWS_NAME), "views"));

//        this.attr(Global.ROOT, S.path.webRoot()#);

        logger.info("root : " + root);
        server = spi(BaseServer.class);
        server.pond(this);
        //TODO ADD CONFIG
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

    public Pond _static(String dir) {
        server.installStatic(server.staticFileServer(dir));
        return this;
    }

    /**
     * Returns a MW that handle session
     * see more at com.shuimin.pond.core.mw.session.Session
     */
    public SessionInstaller useSession () {
        if ( this.sessionManager == null) {
            this.sessionManager = new SessionManager(this);
        }
        return new SessionInstaller(this.sessionManager);
    }

    /**
     * get Session
     */
    Session session( Ctx ctx ) {
        if ( this.sessionManager == null ) {
            throw new RuntimeException( "Please use Pond.before(pond.useSession()) first" );
        }
        return this.sessionManager.get( ctx );
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

    public Pond loadConfigFromCmdLine(String[] args) {
        this.config.readFromFile(args);
        return this;
    }

    /**
     * Load attributes from properties
     */
    public Pond loadConfig( Properties conf ) {
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
     * In this case, the port has been set.
     * @return
     */
    public Pond start() {
        listen(config.getInt(Config.PORT));
        return this;
    }

    public Pond listen(int port) {
        logger.info("Starting server...");
        //append dispatcher to the chain
        List<Mid> mids = new LinkedList<>(before);
        mids.add(rootRouter);

        server.installHandler((req, resp) ->
            executor.exec( new Ctx(req, resp, this, mids),
                Collections.emptyList()));

        server.listen(port);
        logger.info("Server binding port: " + port);
        return this;
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
     */
    private Pond _init() {
        return this;
    }

    public <E> E spi(Class<E> s) {
        E e = SPILoader.service(s);
        if (e == null)
            throw new NullPointerException(s.getSimpleName() + "not found");
        else if ( e instanceof EnvSPI ) {
            ((EnvSPI) e).env(this.config);
        }
        logger.info("Get " + 
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
