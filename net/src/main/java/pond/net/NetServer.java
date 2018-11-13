package pond.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import pond.common.PATH;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.Ctx;
import pond.core.CtxFlowProcessor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class NetServer implements Server {

    final ServerConfig config;

    private Function.F0<Integer> getPort;

    static {
        logger.info("STATIC CONFIGURATION:");

        String root = PATH.classpathRoot();
        logger.info("CLASS ROOT:" + root);

        logger.info("DEBUG INFO");
        init_dbg();

        String webroot = PATH.detectWebRootPath();
        logger.info("WEB ROOT:" + webroot);

        S.config.set(NetServer.class, CONFIG_CLASS_ROOT, root);
        S.config.set(NetServer.class, CONFIG_WEB_ROOT, webroot);

        logger.info("root : " + root);
    }


    public NetServer(ServerConfig config){
        this.config = config;
        getPort = config::port;
    }

    public NetServer(ServerConfig.ServerConfigBuilder builder){
        this(builder.build());
    }

    /**
     * open debug
     */
    private static void init_dbg() {
        if ("true".equals(System.getProperty("pond.debug"))) {
            //read all debug class message from system properties
            String classes = System.getProperty("pond.debug_classes");
            S._debug_on(S._for(classes.split("[:,t]")).map(cls -> {
                try {
                    return S._tap(Class.forName(cls), S::echo);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }).compact().joinArray(new Class[0]));
        }
    }


//    private int port() {
//        return S._tap(
//            Integer.parseInt(S.avoidNull(S.config.get(Server.class, Server.PORT), "8333")),
//            port -> logger.info(String.format("USING PORT %s", port)));
//    }

    private int backlog() {
        return S._tap(Integer.parseInt(S.avoidNull(S.config.get(Server.class, Server.SO_BACKLOG), "1024")),
            backlog -> logger.info(String.format("USING BACKLOG %s", backlog)));
    }

    private boolean keepAlive() {
        return S._tap(Boolean.parseBoolean(S.avoidNull(S.config.get(Server.class, Server.SO_KEEPALIVE), "true")),
            b -> {
                if (b) logger.info("USING keepAlive");
            });
    }


    ChannelFuture serverChannelFuture;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ServerBootstrap b;

    @Override
    public Future listen() {

        //init Netty
        // Configure the server.
        //strictly 1-selector model
        bossGroup = config.getBossGroup().apply();
        workerGroup = config.getWorkerGroup().apply();
        b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        config.options().apply(b);
        b.channel(NioServerSocketChannel.class);
        b.handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(config.toChannelHandler());

        // Start the server.
        int port = getPort.apply();
        logger.info("USING PORT:" + port);
        return CompletableFuture.supplyAsync(() -> {
            try {
                serverChannelFuture =  b.bind(port).sync();

                serverChannelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
            return serverChannelFuture;
        }).handle((f, ex) ->{
            if(f!= null)return f;
            else {
                ex.printStackTrace();
                return null;
            }
        });

    }

    @Override
    public Future listen(int port) throws InterruptedException {
        getPort = () -> port;
        return listen();
    }

    @Override
    public Future stop(Callback<Future> listener) throws Exception {
        assert serverChannelFuture != null;
        logger.info("Closing server...");

//
//    return serverChannelFuture.channel().close().addListener(future -> {
//      futureCallback.apply(future);
//      logger.info("Server closed!");
//    });

        workerGroup.shutdownGracefully();
        return bossGroup.shutdownGracefully();
    }
}
