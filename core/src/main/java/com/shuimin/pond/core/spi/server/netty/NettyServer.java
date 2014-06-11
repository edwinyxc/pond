package com.shuimin.pond.core.spi.server.netty;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.Response;
import com.shuimin.pond.core.spi.BaseServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyServer implements BaseServer {

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ChannelFuture severFuture;
    private Callback.C2<Request, Response> handler = (req, resp) -> {
        S.echo("EMPTY SERVER");
    };

    @Override
    public void listen(int port) {

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline()
                                    .addLast("codec", new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(1048576))
                                    .addLast(new HttpContentCompressor())
                                    .addLast("chunkedWriter", new ChunkedWriteHandler())
                                    .addLast(new HttpServerHandler(handler));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            severFuture = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen,
            // but you can do that to gracefully
            // shut down your server.
            severFuture.channel().closeFuture().sync();//block here?
        } catch (InterruptedException ex) {
            S._lazyThrow(ex);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

        throw new UnsupportedOperationException("Not supported yet.");
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stop() {
        try {
            severFuture.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            S._lazyThrow(ex);
        }
    }

    @Override
    public void installHandler(Callback.C2<Request, Response> handler) {
        this.handler = handler;
    }

}
