package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.*;
import pond.common.f.Callback;
import pond.common.spi.JsonService;
import pond.web.spi.BaseServer;
import pond.web.spi.StaticFileServer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Main Class
 */
public final class Pond implements RouterAPI {

  //configurations
  /***** Internal ****/
  /**
   *
   */
  public final static String CONFIG_FILE_NAME = "config";

  /**
   * web root
   */
  public final static String CONFIG_WEB_ROOT = "web_root";

  /**
   * class root
   */
  public final static String CONFIG_CLASS_ROOT = "class_root";

  /**
   *
   */
  public final static String CONFIG_SO_BACKLOG = "so_backlog";

  public final static String CONFIG_SO_KEEPALIVE = "so_keepalive";

  /*** Basic ***/

  /**
   * listening port
   */
  public final static String PORT = "port";

  /**
   * ssl function
   */
  public final static String ENABLE_SSL = "enable_ssl";

  /**
   * list dir when no index file found (else throw 404)
   */
  public final static String STATIC_SERVER_ENABLE_LISTING_DIR = "static_server_enable_listing_dir";

  /**
   * index file when access uri endsWith "/"
   */
  public final static String STATIC_SERVER_INDEX_FILE = "static_server_index_file";

  /**
   * Date format for cache control
   */
  public final static String STATIC_SERVER_HTTP_DATE_FORMAT = "static_server_http_date_format";

  /**
   * gmt timezone for http server
   */
  public final static String STATIC_SERVER_HTTP_DATE_GMT_TIMEZONE = "static_server_http_date_gmt_timezone";

  /**
   * seconds for cache-control
   */
  public final static String STATIC_SERVER_HTTP_CACHE_SECONDS = "static_server_http_cache_seconds";


  //END of configurations

  static Logger logger = LoggerFactory.getLogger(Pond.class);

  private BaseServer server;
  private Router rootRouter;

  StaticFileServer staticFileServer;

  //Before the routing chain
  final List<Mid> before = new LinkedList<>();

  // Executor
  final CtxExec ctxExec = new CtxExec();

  //After the routing chain

//  private Map<String, ViewEngine> viewEngines
//      = new HashMap<String, ViewEngine>() {{
//    //do not put any code here
//  }};

  public Pond before(Mid mid) {
    before.add(mid);
    return this;
  }

//    public Pond after(Mid mid) {
//        after.add(mid);
//        return this;
//    }

  private Pond() {

    logger.info("POND:");

    String root = PATH.classpathRoot();
    logger.info("CLASS ROOT:" + root);

    String webroot = PATH.detectWebRootPath();
    logger.info("WEB ROOT:" + webroot);

    String cfgFileName = S.config.get(Pond.class, CONFIG_FILE_NAME);

    if (STRING.notBlank(cfgFileName)) {
      //map properties
      File configFile = new File(root + File.separator + cfgFileName);

      if (configFile.exists() && configFile.canRead()) {
        loadConfig(FILE.loadProperties(configFile));
      } else {
        logger.info("config file:" + cfgFileName + "not found. using default values");
      }

    }

    S.config.set(Pond.class, CONFIG_CLASS_ROOT, root);
    S.config.set(Pond.class, CONFIG_WEB_ROOT, webroot);


//      this.attr(Global.ROOT, S.path.webRoot()#);

    logger.info("root : " + root);
    server = spi(BaseServer.class);
    server.pond(this);

    //TODO ADD CONFIG
    //TODO CHANGE CONFIG LAYER
    //router
    rootRouter = new Router();

//    //engine
//    ViewEngine vg = spi(ViewEngine.class);
//    try {
//      vg.configViewPath(config.get(Config.VIEWS_PATH));
//    } catch (Exception e) {
//      debug(e.getMessage());
//    }

//    viewEngines.put("default", vg);
    logger.info("Installing Handler");
    //init handler
    logger.info("... Finished");

  }

  public Pond listen(int port) {
    this.config(BaseServer.PORT, String.valueOf(port));
    listen();
    return this;
  }

  public void listen() {

    logger.info("Starting server...");
    //append dispatcher to the chain
    LinkedList<Mid> mids = new LinkedList<>(before);
    mids.add(rootRouter);

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

  }

  public Mid _static(String dir) {
    if (staticFileServer == null) {
      staticFileServer = SPILoader.service(StaticFileServer.class);
    }
    return staticFileServer.watch(dir);
  }


  /**
   * Returns JsonService
   */
  public JsonService json() {
    return SPILoader.service(JsonService.class);
  }

  /**
   * Load attributes from properties
   */
  public Pond loadConfig(Properties conf) {
    S._assert(conf);
    for (Map.Entry e : conf.entrySet()) {
      logger.info("Reading conf: " + e.getKey() + "=" + e.getValue());
      S.config.set(e.getKey().toString(), String.valueOf(e.getValue()));
    }
    return this;
  }

  /**
   * Custom initialization
   */
  @SafeVarargs
  public static Pond init(Callback<Pond>... configs) {
    Pond pond = new Pond();

    for (Callback<Pond> conf : configs) {
      conf.apply(pond);
    }
    return pond;
  }

  public static String _ignoreLastSlash(String path) {
    if (!"/".equals(path) && path.endsWith("/"))
      return path.substring(0, path.length() - 1);
    return path;
  }

  public static void debug(Object s) {
    S._debug(logger, log -> log.debug(S.dump(s)));
  }


//  /**
//   * get absolute path relative to g.web_root
//   *
//   * @param path input relative path
//   * @return absolute path
//   */
//  public String pathRelWebRoot(String path) {
//    String root = config.get(Config.ROOT_WEB);
//    if (path == null) return null;
//    if (PATH.isAbsolute(path)) {
//      return path;
//    }
//    return root + File.separator + path;
//  }
//
//  /**
//   * get absolute path relative to g.root
//   *
//   * @param path input relative path
//   * @return absolute path
//   */
//  public String pathRelRoot(String path) {
//    String root = config.get(Config.ROOT);
//    if (path == null) return null;
//    if (PATH.isAbsolute(path)) {
//      return path;
//    }
//    return root + File.separator + path;
//  }


//  public ViewEngine viewEngine(String ext) {
//    ViewEngine ret = viewEngines.get(ext);
//    if (ret == null) ret = viewEngines.get("default");
//    return ret;
//  }
//
//  public Pond viewEngine(String ext, ViewEngine viewEngine) {
//    viewEngines.put(ext, viewEngine);
//    return this;
//  }

  public static String config(String name) {
    return S.config.get(Pond.class, name);
  }

  public static void config(String name, String config) {
    S.config.set(Pond.class, name, config);
  }


  /**
   * Open the debug mode for Pond
   */
  public Pond debug() {

    S._debug_on(Pond.class,
                BaseServer.class,
                StaticFileServer.class);

    return this;
  }


  public <E> E spi(Class<E> s) {
    E e = SPILoader.service(s);
    logger.info("SPI-INJECT " + s.getSimpleName() + " : "
                    + e.getClass().getCanonicalName());
    return e;
  }

  @Override
  public RouterAPI use(int mask, String path, Mid... mids) {
    return this.rootRouter.use(mask, path, mids);
  }

  @Override
  public RouterAPI use(String path, Router router) {
    return this.rootRouter.use(path, router);
  }

  @SuppressWarnings("unchecked")
  public void stop() {
    try {
      //sync
      server.stop(Callback.NOOP).get();
    } catch (Exception e) {
      Pond.logger.error(e.getMessage(), e);
    }
  }

}
