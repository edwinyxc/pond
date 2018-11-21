package pond.web.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.core.Ctx;
import pond.core.CtxHandler;
import pond.net.NetServer;
import pond.net.ServerConfig;
import pond.web.router.Router;

import java.util.LinkedList;

/**
 * Http 1.1 ServerConfig
 */
public class HttpConfigBuilder extends ServerConfig.ServerConfigBuilder {
    public static Logger logger = LoggerFactory.getLogger(HttpServerInitializer.class);
    private LinkedList<CtxHandler<? extends Ctx>> handlers = new LinkedList<>();

    private boolean _isHeaderCaseSensitive = false;
    public boolean isHeaderCaseSensitive(){
        return _isHeaderCaseSensitive;
    }

    /**
     *
     * @return
     */
    @Override
    protected ChannelHandler init() {
        return new HttpServerInitializer();
    }

    @SuppressWarnings("unchecked")
    public HttpConfigBuilder handler(CtxHandler<HttpCtx> handler) {
        this.handlers.add(handler);
        return this;
    }

    public HttpConfigBuilder handlers(Iterable<CtxHandler<HttpCtx>> handlers) {
        S._for(handlers).each(this::handler);
        return this;
    }

    public HttpConfigBuilder clean(){
        handlers.clear();
        return this;
    }


    public HttpConfigBuilder debug() {

        S._debug_on(
            NetServer.class,
            Router.class,
            Ctx.class,
            CtxHandler.class);
        return this;
    }

    /**
     * Open the debug mode for Pond
     */
    public HttpConfigBuilder debug(Class... c) {

        S._debug_on(c);
        return this;
    }



    class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            //TODO --- manual distinguish between static & dynamic
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new HttpCtxBuilder(HttpConfigBuilder.this,handlers).build());
        }
    }
}

