package pond.net;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import pond.core.Ctx;

import java.net.SocketAddress;

public interface CtxNet extends Ctx {

    default ChannelHandlerContext chctx(){
        return (ChannelHandlerContext) this.properties().get("chctx");
    }

    default ChannelPipeline nettyChannelPipeline() {
        return chctx().pipeline();
    }

    default ChannelFuture write(Object t) {
        return chctx().write(t);
    }

    default SocketAddress remoteAddress() {
        return chctx().channel().remoteAddress();
    }

    default <T extends CtxNet> T flush(){
        chctx().flush();
        return (T) this;
    }

    static <T extends CtxNet> T adapt(T ctx, ChannelHandlerContext channelHandlerContext){
        ctx.delegate().properties().put("chctx", channelHandlerContext);
        return ctx;
    }


}
