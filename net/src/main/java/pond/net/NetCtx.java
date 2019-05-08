package pond.net;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import pond.core.Ctx;

import java.net.SocketAddress;

public interface NetCtx extends Ctx {

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

    default <T extends NetCtx> T flush(){
        chctx().flush();
        return (T) this;
    }

    static <T extends NetCtx> T adapt(T ctx, ChannelHandlerContext channelHandlerContext){
        ctx.delegate().properties().put("chctx", channelHandlerContext);
        return ctx;
    }


}
