package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.PATH;
import pond.common.S;
import pond.common.SPILoader;
import pond.common.f.Callback;
import pond.web.spi.BaseServer;
import pond.web.spi.SessionStore;
import pond.web.spi.StaticFileServer;


/**
 * Main Class
 */
public final class Pond extends Router {

  //configurations
  /***** Internal ****/
//  public final static String CONFIG_FILE_NAME = "config";

  /**
   * web root
   */
  public final static String CONFIG_WEB_ROOT = "web_root";

  /**
   * class root
   */
  public final static String CONFIG_CLASS_ROOT = "class_root";


  /*** Basic ***/

  /**
   * listening port
   */
  public final static String PORT = "port";


  //END of configurations

  static Logger logger = LoggerFactory.getLogger(Pond.class);

  private BaseServer server;

  StaticFileServer staticFileServer;// Executor
  final CtxExec ctxExec = new CtxExec();


  private Pond() {

    logger.info("POND:");

    String root = PATH.classpathRoot();
    logger.info("CLASS ROOT:" + root);

    String webroot = PATH.detectWebRootPath();
    logger.info("WEB ROOT:" + webroot);

    S.config.set(Pond.class, CONFIG_CLASS_ROOT, root);
    S.config.set(Pond.class, CONFIG_WEB_ROOT, webroot);

    logger.info("root : " + root);
    server = SPILoader.service(BaseServer.class);
    server.pond(this);

//    rootRouter = new Router();

  }

  public Pond listen(int port) {
    S.config.set(BaseServer.class, BaseServer.PORT, String.valueOf(port));
    listen();
    return this;
  }

  public Pond cleanAndBind(Callback<Pond> config) {
    super.clean();
    config.apply(this);
    this.bindLastMids();
    return this;
  }

  @Override
  @Deprecated
  /**
   * @link pond.web.Pond#cleanAndBind
   */
  public void clean() {
    //do nothing
  }

  private void bindLastMids() {
    //add at last
    this.use(Mids.FORCE_CLOSE);
  }

  public void listen() {

    logger.info("Starting server...");

    bindLastMids();

    server.registerHandler((req, resp) -> {
      Ctx ctx = new Ctx(req, resp, this);
      ctxExec.execAll(ctx, this);
    });

    try {
      server.listen().get();
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

//  /**
//   * Load attributes from properties
//   */
//  public Pond loadConfig(Properties conf) {
//    S._assert(conf);
//    for (Map.Entry e : conf.entrySet()) {
//      logger.info("Reading conf: " + e.getKey() + "=" + e.getValue());
//      S.config.set(e.getKey().toString(), String.valueOf(e.getValue()));
//    }
//    return this;
//  }

  public static Pond init() {
    return init(Callback.noop());
  }

  /**
   * Custom initialization
   */
  public static Pond init(Callback<Pond> config) {
    Pond pond = new Pond();
    config.apply(pond);
    return pond;
  }

  public static String _ignoreLastSlash(String path) {
    if (!"/".equals(path) && path.endsWith("/"))
      return path.substring(0, path.length() - 1);
    return path;
  }

  /**
   * Open the debug mode for Pond
   */
  public Pond debug() {

    S._debug_on(Pond.class,
                BaseServer.class,
                Router.class,
                StaticFileServer.class,
                SessionStore.class);

    return this;
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
