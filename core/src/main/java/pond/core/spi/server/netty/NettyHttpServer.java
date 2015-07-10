package pond.core.spi.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpUtil;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.f.Callback;
import pond.core.Request;
import pond.core.Response;
import pond.core.spi.BaseServer;
import pond.core.spi.server.AbstractServer;
import sun.nio.cs.US_ASCII;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import static pond.common.f.Callback.C2;


public class NettyHttpServer extends AbstractServer {

    public NettyHttpServer() {

    }

    // configuration getters
    private boolean ssl() {
        return S._tap(Boolean.TRUE.equals(env(BaseServer.SSL)), b -> {
            if (b) {
                //TODO
                logger.warn("SSL is not supported");
                //logger.info("USING SSL");
            }
        });
    }

    private int port() {
        return S._tap(ssl() ? 443 : Integer.parseInt((String) S.avoidNull(env(BaseServer.PORT), "8080")),
                port -> logger.info(String.format("USING PORT %s", port)));
    }

    private int backlog() {
        return S._tap(Integer.parseInt((String) S.avoidNull(env(BaseServer.BACK_LOG), "128")),
                backlog -> logger.info(String.format("USING BACKLOG %s", backlog)));
    }

    private boolean keepAlive() {
        return S._tap(Boolean.TRUE.equals(env("keepAlive")),
                b -> {
                    if (b) logger.info("USING keepAlive");
                });
    }

    //util
    @Deprecated
    SslContext buildSslContext() throws Exception {
        if (ssl()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
        }
        return null;
    }

    class NettyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        List<Callback<ChannelHandlerContext>> channelInactiveHooks = new LinkedList<>();
        List<Callback<ChannelHandlerContext>> channelActiveHooks = new LinkedList<>();
        List<Callback.C2<ChannelHandlerContext, Throwable>> exceptionCaughtHooks = new LinkedList<>();

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }


        @Override
        protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

//            if(HttpHeaderUtil.is100ContinueExpected(msg)){
//                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
//                        HttpResponseStatus.CONTINUE));
//                return;
//            }

            if (!msg.decoderResult().isSuccess()) {
                ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.BAD_REQUEST));
                return;
            }

            NettyReqWrapper reqWrapper = new NettyReqWrapper(ctx, msg, NettyHttpServer.this);
            NettyRespWrapper respWrapper = new NettyRespWrapper(ctx, msg, NettyHttpServer.this);

            //TODO release any refs
            //check for memory leak
            Runnable actor = NettyHttpServer.super.actor(reqWrapper,respWrapper);
            NettyHttpServer.super.executor.submit(actor);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            if (ctx.channel().isActive()) {
                //TODO sendError
                ctx.close();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            S._for(channelInactiveHooks).each(hook -> hook.apply(ctx));
        }
    }


    public void listen() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            //max concurrent income connections in queue
            b.option(ChannelOption.SO_BACKLOG, backlog())
                    .option(ChannelOption.SO_REUSEADDR, true);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            //FIXME combine with the chunked writer
                            //pipeline.addLast(new HttpContentCompressor() );
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new NettyHttpHandler());
                        }
                    })

                            //TODO configurations here
                            //TODO interceptors here
                            //TODO fail-back http server here?
                            //TODO baseServer here (discard jetty & the oio or recreate a abstraction FP(req,res) layer?)
                            //TODO
                    .childOption(ChannelOption.SO_KEEPALIVE, keepAlive())
            ;

            ChannelFuture f = b.bind(port()).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
