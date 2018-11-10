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
import pond.common.f.Callback;
import pond.core.Ctx;
import pond.core.Executable;
import pond.net.ServerConfig;
import pond.web.CtxHandler;

import java.util.LinkedList;

/**
 * Http 1.1 ServerConfig
 */
public class HttpConfigBuilder extends ServerConfig.ServerConfigBuilder {
    public static Logger logger = LoggerFactory.getLogger(HttpServerInitializer.class);
    private LinkedList<Executable<? extends Ctx>> handlers = new LinkedList<>();

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
    public HttpConfigBuilder handler(Callback<CtxHttp> handler) {
        if(handler instanceof Executable){
            this.handlers.add((Executable)handler);
        }else {
            this.handlers.add(Executable.of(handler));
        }
        return this;
    }

    public HttpConfigBuilder handlers(Iterable<Callback<CtxHttp>> handlers) {
        S._for(handlers).each(this::handler);
        return this;
    }

    public HttpConfigBuilder clean(){
        handlers.clear();
        return this;
    }

    class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            //TODO --- manual distinguish between static & dynamic
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new CtxHttpBuilder(HttpConfigBuilder.this,handlers).build());
        }
    }
}


