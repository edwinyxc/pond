package pond.net;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import pond.core.Ctx;
import pond.core.CtxBase;

public interface CtxNet extends Ctx {

    default ChannelHandlerContext nettyChannelHandlerContext(){
        return (ChannelHandlerContext) this.properties().get("nettyChannelHandlerContext");
    }

    default ChannelPipeline nettyChannelPipeline() {
        return nettyChannelHandlerContext().pipeline();
    }

    default ChannelFuture write(Object t) {
        return nettyChannelHandlerContext().write(t);
    }

    default <T extends CtxNet> T flush(){
        nettyChannelHandlerContext().flush();
        return (T) this;
    }

    static <T extends CtxNet> T adapt(T ctx, ChannelHandlerContext channelHandlerContext){
        ctx.delegate().properties().put("nettyChannelHandlerContext", channelHandlerContext);
        return ctx;
    }


}
