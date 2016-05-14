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

  private Pond() {

    logger.info("POND:");

    String root = PATH.classpathRoot();
    logger.info("CLASS ROOT:" + root);

    logger.info("DEBUG INFO");
    init_dbg();

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
//    this.bindLastMids();
    return this;
  }

  @Override
  /**
   * @link pond.web.Pond#cleanAndBind
   */
  public void clean() {
    super.clean();
    //do nothing
  }

//  private void bindLastMids() {
//    //add at last
//    this.use(InternalMids.FORCE_CLOSE);
//  }

  public void listen() {

    logger.info("Starting server...");

//    bindLastMids();

    server.registerHandler((req, resp) -> {
      new WebCtx(req, resp, this).execAll(this);
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

  public static Pond init() {
    return init(Callback.noop());
  }

  /**
   * open debug
   */
  private static void init_dbg() {
    if ("true".equals(System.getProperty("pond.debug"))) {
      //read all debug class message from system properties
      String classes = System.getProperty("pond.debug_classes");
      S._debug_on(S._for(classes.split(":")).map(cls -> {
        try {
          return S._tap(Class.forName(cls), S::echo);
        } catch (ClassNotFoundException e) {
          return null;
        }
      }).compact().join());
    }
  }

  /**
   * Custom initialization
   */
  public static Pond init(Callback<Pond> config) {
    Pond pond = new Pond();
    config.apply(pond);
    return pond;
  }

  /**
   * Open the debug mode for Pond
   */
  public Pond debug(Class... c) {

    S._debug_on(Pond.class,
                BaseServer.class,
                Router.class,
                StaticFileServer.class,
                SessionStore.class);

    S._debug_on(c);
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
