//package pond.web;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.Headers;
//import io.netty.handler.codec.http.*;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//import io.netty.util.CharsetUtil;
//import io.netty.util.concurrent.Future;
//import io.netty.util.concurrent.GenericFutureListener;
//import pond.common.S;
//import pond.common.f.Callback;
//
//import javax.net.ssl.SSLException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.Map;
//
//public class NettyHttpClientBuilder {
//
//    final URI URL;
//    final int PORT;
//    final String HOST;
//    final boolean using_ssl;
//    final int SO_TIMEOUT = 30;
//    final Callback.C2<ChannelHandlerContext, FullHttpResponse> handler;
//    private final SimpleChannelInboundHandler<FullHttpResponse> innerHandler;
//
//    public NettyHttpClientBuilder(String url, Callback.C2<ChannelHandlerContext, FullHttpResponse> handler) throws URISyntaxException {
//        URL = new URI(url);
//        String protocol = URL.getScheme() == null ? "http" : URL.getScheme();
//        if(URL.getHost() == null) throw new URISyntaxException("Host String must not be null or empty", "");
//        HOST = URL.getHost();
//
//        protocol = protocol.toLowerCase();
//        if(protocol.endsWith("https")){
//            using_ssl = true;
//            PORT = 443;
//        } else {
//            using_ssl = false;
//            PORT = 80;
//        }
//
//        this.handler = handler;
//        this.innerHandler = new SimpleChannelInboundHandler<FullHttpResponse>() {
//            @Override
//            protected void messageReceived(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse) throws Exception {
//                //TODO
//                S.echo("Full Http Request Got: ", fullHttpResponse.status().toString());
//                S.echo("Headers:");
//                fullHttpResponse.headers().forEachEntry(entry -> {
//                    S.echo(entry.getKey(), "=", entry.getValue());
//                    return true;
//                });
//                S.echo("PayLoad:", fullHttpResponse.content().toString(CharsetUtil.UTF_8));
//            }
//
//            @Override
//            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//                //TOOD
//                cause.printStackTrace();
//                ctx.close();
//            }
//        };
//    }
//
//    public ChannelFuture connect() throws SSLException {
//
//        final SslContext sslCtx;
//        if (using_ssl) {
//           sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
//        } else {
//            sslCtx = null;
//        }
//
//        EventLoopGroup group = new NioEventLoopGroup();
//        Bootstrap bootstrap = new Bootstrap();
//
//        bootstrap.group(group)
//                .channel(NioSocketChannel.class)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    protected void initChannel(SocketChannel socketChannel) throws Exception {
//                        ChannelPipeline pipeline = socketChannel.pipeline();
//                        if(sslCtx != null) {
//                            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc(), HOST, PORT));
//                        }
//
//                        pipeline.addLast(
//                                new HttpClientCodec(),
//                                new HttpObjectAggregator(65536),
//                                NettyHttpClientBuilder.this.innerHandler
//                        );
//
//                    }
//                });
//        return bootstrap.connect(this.HOST, this.PORT);
//    }
//
//
//    public static void main(String args[]) {
//        try {
//            ChannelFuture f = new NettyHttpClientBuilder("http://www.baidu.com/", null).connect();
//            Channel ch = f.sync().channel();
//            //build http Request
//            HttpRequest request = new DefaultFullHttpRequest(
//                    HttpVersion.HTTP_1_1,
//                    HttpMethod.GET,
//                    "/"
//            );
//            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
//            S.echo("Sending Request", request.toString());
//
//            ch.writeAndFlush(request);
//            ch.closeFuture().sync();
//        } catch (SSLException e) {
//            e.printStackTrace();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
