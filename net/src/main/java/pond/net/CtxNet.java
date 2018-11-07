package pond.net;

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

    default void nettyWrite(Object t) {
        nettyChannelHandlerContext().write(t);
    }

    default void flush(){
        nettyChannelHandlerContext().flush();
    }


    class CtxNetAdapter{

        final ChannelHandlerContext nettyChannelHandlerContext;

        public CtxNetAdapter(ChannelHandlerContext nettyChannelHandlerContext) {
            this.nettyChannelHandlerContext = nettyChannelHandlerContext;
        }

        public CtxNet adapt(Ctx ctx){
            ctx.delegate().properties().put("nettyChannelHandlerContext", nettyChannelHandlerContext);
            return (CtxNet) ctx;
        }
    }

    class CtxNetBuilder{
        final ChannelHandlerContext nettyChannelHandlerContext;

        public CtxNetBuilder(ChannelHandlerContext nettyChannelHandlerContext) {
            this.nettyChannelHandlerContext = nettyChannelHandlerContext;
        }

        public CtxNet build(){
            return new CtxNetAdapter(this.nettyChannelHandlerContext).adapt(CtxBase::new);
        }
    }
}
