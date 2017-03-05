package pond.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import pond.common.Convert;
import pond.common.S;
import pond.common.f.Callback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class NettyHttpServer implements BaseServer {

  public final static String EVENT_GROUP_BOSS_GROUP_COUNT = "boss_group_count";

  public final static String EVENT_GROUP_WORKER_GROUP_COUNT = "worker_group_count";

  public final static String EXECUTOR_THREAD_POOL_SIZE = "executor_thread_pool_size";

  private Pond pond;
  private CtxHandler handler;

  protected CtxHandler handler() {
    return this.handler;
  }

  @Override
  public void pond(Pond pond) {
    this.pond = pond;
  }

  @Override
  public Pond pond() {
    return pond;
  }

  @Override
  public void registerHandler(CtxHandler handler) {
    this.handler = handler;
  }


  public NettyHttpServer() { }

//  // configuration getters
//  private boolean ssl() {
//    return S._tap(Boolean.parseBoolean(Pond.config(Pond.ENABLE_SSL)), b -> {
//      if (b) {
//        //TODO
//        logger.warn("SSL is not supported");
//        //logger.info("USING SSL");
//      }
//    });
//  }

  private int port() {
    return S._tap(
        Integer.parseInt(S.avoidNull(S.config.get(BaseServer.class, BaseServer.PORT), "8333")),
        port -> logger.info(String.format("USING PORT %s", port)));
  }

  private int backlog() {
    return S._tap(Integer.parseInt(S.avoidNull(S.config.get(BaseServer.class, BaseServer.SO_BACKLOG), "1024")),
                  backlog -> logger.info(String.format("USING BACKLOG %s", backlog)));
  }

  private boolean keepAlive() {
    return S._tap(Boolean.parseBoolean(S.avoidNull(S.config.get(BaseServer.class, BaseServer.SO_KEEPALIVE), "true")),
                  b -> {
                    if (b) logger.info("USING keepAlive");
                  });
  }


  ChannelFuture serverChannelFuture;
  EventLoopGroup bossGroup;
  EventLoopGroup workerGroup;

  @Override
  public Future listen() {

    //since we only listen on single port
    bossGroup = new NioEventLoopGroup(
        S.avoidNull(Convert.toInt(
            S.config.get(NettyHttpServer.class,
                         NettyHttpServer.EVENT_GROUP_BOSS_GROUP_COUNT)
        ),
//                1)
                Runtime.getRuntime().availableProcessors() + 1)
    );
    workerGroup = new NioEventLoopGroup();

    ServerBootstrap b = new ServerBootstrap();

    //max concurrent income connections in queue
    b.option(ChannelOption.SO_BACKLOG, backlog())
        .option(ChannelOption.SO_REUSEADDR, true)
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

//        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) throws Exception {
            //TODO --- manual distinguish between static & dynamic
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new NettyHttpHandler(handler()));
          }
        }).childOption(ChannelOption.SO_KEEPALIVE, keepAlive())
          .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
    ;
//        .childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
//        .childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
    ;

    return S._tap(b.bind(port()), f -> new Thread(() -> {
      try {
        serverChannelFuture = f.sync();
        serverChannelFuture.channel().closeFuture().sync();
      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException(e);
      } finally {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
      }
    }).start());

  }

  @Override
  public Future stop(Callback<Future> futureCallback) throws Exception {

    S._assertNotNull(serverChannelFuture);

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