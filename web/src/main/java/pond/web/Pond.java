package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.FILE;
import pond.common.PATH;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.spi.JsonService;
import pond.common.spi.SPILoader;
import pond.web.spi.BaseServer;

import java.io.File;
import java.util.*;

import static pond.common.S.avoidNull;

/**
 * Main Class
 */
public final class Pond implements RouterAPI {

  static Logger logger = LoggerFactory.getLogger(Pond.class);

  private BaseServer server;
  private Router rootRouter;
  public final Config config = new Config();
  public final Map<String, Object> container = new HashMap<>();

  DefaultStaticFileServer staticFileServer;

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
    String root = PATH.classpathRoot();
    String webroot = PATH.detectWebRootPath();

    //map properties
    File configFile = new File(root + File.separator + Config.CONFIG_FILE_NAME);

    if (configFile.exists() && configFile.canRead()) {
      loadConfig(FILE.loadProperties(configFile));
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
    System.setProperty(BaseServer.PORT, String.valueOf(port));
    listen();
    return this;
  }

  public Pond listen() {

    logger.info("Starting server...");
    //append dispatcher to the chain
    LinkedList<Mid> mids = new LinkedList<>(before);
    mids.add(rootRouter);

    server.handler((req, resp) -> {
      Ctx ctx = new Ctx(req, resp, this, mids);
      ctxExec.exec(ctx);
    });

    try {
      new Thread(server::listen).start();
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
   * Get config
   */
  public String config(String name) {
    return config.get(name);
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
    if (PATH.isAbsolute(path)) {
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
    if (PATH.isAbsolute(path)) {
      return path;
    }
    return root + File.separator + path;
  }


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


  /**
   * DO NOT USE
   * MAY BE DELETED IN FUTURE
   */
  public Pond debug() {
    S._debug_on(Pond.class, BaseServer.class);
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
    if (e instanceof EnvSPI) {
      ((EnvSPI) e).env(this.config);
    }
    logger.info("SPI-INJECT " +
                    s.getSimpleName() + " : " + e.getClass().getCanonicalName());
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

  public void stop() {
    try {
      server.stop(Callback.NOOP);
    } catch (Exception e) {
      Pond.logger.error(e.getMessage(), e);
    }
  }

}
