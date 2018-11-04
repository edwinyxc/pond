package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.PATH;
import pond.common.S;
import pond.common.f.Callback;

import java.util.regex.Pattern;


/**
 * Main Class
 */
public final class Pond implements RouterAPI, CtxHandler {

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


  //END handle configurations

  public static Logger logger = LoggerFactory.getLogger(Pond.class);

  private BaseServer server;

  StaticFileServer staticFileServer;// Executor

  public final Router rootRouter;

  private Pond(Class<? extends Router> routerClass) {
    logger.info("RootRouterClass: " + routerClass.getCanonicalName());
    try {
      rootRouter = routerClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

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
    server = new NettyHttpServer();
    server.pond(this);

//    rootRouter = new Router();

  }

  public Pond listen(int port) {
    S.config.set(BaseServer.class, BaseServer.PORT, String.valueOf(port));
    listen();
    return this;
  }


  public Pond cleanAndBind(Callback<Pond> config) {
    rootRouter.clean();
    config.apply(this);
//    this.bindLastMids();
    return this;
  }

  /**
   * @link pond.web.Pond#cleanAndBind
   */
  public void clean() {
    rootRouter.clean();
    //do nothing
  }

//  private void bindLastMids() {
//    //add at last
//    this.use(InternalMids.FORCE_CLOSE);
//  }

  public void listen() {

    logger.info("Starting server...");

//    bindLastMids();

    server.registerHandler(ctx -> ctx.execAll(this));

    try {
      server.listen().get();
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(S.dump(e.getStackTrace()));
    }

  }

  public Mid _static(String dir) {
    if (staticFileServer == null) {
      staticFileServer = new DefaultStaticFileServer();
    }
    return staticFileServer.watch(dir);
  }


  /**
   * open debug
   */
  private static void init_dbg() {
    if ("true".equals(System.getProperty("pond.debug"))) {
      //read all debug class message from system properties
      String classes = System.getProperty("pond.debug_classes");
      S._debug_on(S._for(classes.split("[:,t ]")).map(cls -> {
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
  public static Pond init(Class<? extends Router> clazz, Callback<Pond> config) {
    Pond pond = new Pond(clazz);
    config.apply(pond);
    return pond;
  }

  public static Pond init() {
    return init(Router.class, Callback.noop());
  }

  public static Pond init(Class<? extends Router> clazz) {
    return init(clazz, Callback.noop());
  }

  public static Pond init(Callback<Pond> config) {
    return init(Router.class, config);
  }

  public Pond debug() {

    S._debug_on(Pond.class,
                BaseServer.class,
                Router.class,
                StaticFileServer.class,
                SessionStore.class,
                Ctx.class,
                CtxHandler.class);

    return this;
  }

  /**
   * Open the debug mode for Pond
   */
  public Pond debug(Class... c) {

//    S._debug_on(Pond.class,
//                BaseServer.class,
//                Router.class,
//                StaticFileServer.class,
//                SessionStore.class,
//                Ctx.class,
//                CtxHandler.class);

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

  @Override
  public void apply(Ctx t) {
    rootRouter.apply(t);
  }

  @Override
  public void configRoute(Route route, CtxHandler handler) {
    rootRouter.configRoute(route, handler);
  }

  @Override
  public Router use(int mask, Pattern path, String rawDef, String[] inUrlParams, CtxHandler[] handlers) {
    return rootRouter.use(mask, path, rawDef, inUrlParams, handlers);
  }

  @Override
  public Router otherwise(CtxHandler... mids) {
    return rootRouter.otherwise(mids);
  }
}
