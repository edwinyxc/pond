package pond.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;


import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import static io.netty.handler.codec.http.HttpHeaderUtil.is100ContinueExpected;

/**
 * Created by ed on 4/7/16.
 */
public class NettyPerformance {

  private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
  public void run() {

    // Configure the server.
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.option(ChannelOption.SO_BACKLOG, 1024);
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

                  ChannelPipeline p = ch.pipeline();

                  // Uncomment the following line if you want HTTPS
                  //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
                  //engine.setUseClientMode(false);
                  //p.addLast("ssl", new SslHandler(engine));

                  p.addLast("codec", new HttpServerCodec());
                  p.addLast("handler", new ChannelHandlerAdapter(){

                    @Override
                    public void channelReadComplete(ChannelHandlerContext ctx) {
                      ctx.flush();
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                      if (msg instanceof HttpRequest) {
                        HttpRequest req = (HttpRequest) msg;

                        if (is100ContinueExpected(req)) {
                          ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                        }
                        boolean keepAlive = HttpHeaderUtil.isKeepAlive(req);
                        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
                        response.headers().set(CONTENT_TYPE, "text/plain");
                        response.headers().set(CONTENT_LENGTH, String.valueOf(response.content().readableBytes()));

                        if (!keepAlive) {
                          ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                        } else {
                          response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                          ctx.write(response);
                        }
                      }
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                      cause.printStackTrace();
                      ctx.close();
                    }
                  });
                }

              });

      Channel ch = b.bind(9090).sync().channel();
      ch.closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }


  public static void main(String[] args){
    new NettyPerformance().run();
  }
}
