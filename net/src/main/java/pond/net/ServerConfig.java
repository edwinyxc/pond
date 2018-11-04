package pond.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;

public interface ServerConfig {

    ChannelHandler toChannelHandler();
    int port();
    Function.F0<EventLoopGroup> getBossGroup();
    Function.F0<EventLoopGroup> getWorkerGroup();
    Callback<ServerBootstrap> options();



    abstract class ServerConfigBuilder {

        protected abstract ChannelHandler init();

        int _port =  S._tap(Integer.parseInt(S.avoidNull(S.config.get(Server.class, Server.PORT), "8333")),
            port -> Server.logger.info(String.format("DEFAULT PORT %s", port)));
        public ServerConfigBuilder  port(int port){
            _port = port;
            return this;
        }

        Function.F0<EventLoopGroup> _bossG =  () -> new NioEventLoopGroup(1);
        public ServerConfigBuilder boosGroup(Function.F0<EventLoopGroup> group) {
            _bossG = group;
            return this;
        }

        Function.F0<EventLoopGroup> _workerG = NioEventLoopGroup::new;
        public ServerConfigBuilder workerGroup(Function.F0<EventLoopGroup> group){
            _workerG = group;
            return this;
        }

        Callback<ServerBootstrap> _b_option_cb = b -> {
                b.option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        };
        public <T> ServerConfigBuilder channelOption(ChannelOption<T> option, T t) {
            _b_option_cb = b -> {_b_option_cb.apply(b); b.option(option, t);};
            return this;
        }


        ServerConfig build(){
            return new ServerConfig() {
                @Override
                public ChannelHandler toChannelHandler() {
                    return init();
                }

                @Override
                public int port() {
                    return _port;
                }

                @Override
                public Function.F0<EventLoopGroup> getBossGroup() {
                    return _bossG;
                }

                @Override
                public Function.F0<EventLoopGroup> getWorkerGroup() {
                    return _workerG;
                }

                @Override
                public Callback<ServerBootstrap> options() {
                    return _b_option_cb;
                }
            };
        }
    }


    ServerConfigBuilder WELCOME = new ServerConfigBuilder() {
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
}
