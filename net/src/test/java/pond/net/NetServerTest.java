package pond.net;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import pond.common.S;

public class NetServerTest {

    static ServerConfig.ServerConfigBuilder WELCOME = new ServerConfig.ServerConfigBuilder() {
        @Override
        protected ChannelHandler init() {
            return new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new LoggingHandler(LogLevel.INFO));
                    p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                    p.addLast(new LineBasedFrameDecoder(8192));
                    p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                    p.addLast(new ChunkedWriteHandler());
                    p.addLast(new SimpleChannelInboundHandler<String>(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            super.channelActive(ctx);
                            ctx.writeAndFlush("Pond is a fun");
                        }

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                            ChannelFuture cf = ctx.write(
                                    "Hello my friend! your msg is: ");
                            if(!cf.isSuccess()){
                                S.echo(cf.cause());
                            }
                            ctx.writeAndFlush(msg);
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) {
                            //ctx.flush();
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                            // Close the connection when an exception is raised.
                            cause.printStackTrace();
                            ctx.close();
                        }
                    });
                }
            };
        }
    };
    public static void main(String[] args) {
        new Thread(
                new NetServer(
                        new ServerConfig.ServerConfigBuilder() {
                            @Override
                            protected ChannelHandler init() {
                                return new EchoServerHandler();
                            }
                        }.port(8080)
                )
        ).start();
        new Thread(
                new NetServer(
                        WELCOME
                        .port(9090)
                )
        ).start();
    }

}