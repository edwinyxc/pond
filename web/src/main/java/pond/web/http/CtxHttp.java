package pond.web.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.CookieDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.AsciiString;
import pond.common.S;
import pond.core.Ctx;
import pond.core.Executable;
import pond.net.CtxNet;
import pond.web.CtxHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CtxHttp extends CtxNet {
    class Keys {
        public static final Ctx.Entry<CtxHttpBuilder> Builder = new Ctx.Entry<>("Builder");
        public static final Ctx.Entry<HttpRequest> NettyRequest = new Ctx.Entry<>("NettyRequest");
        public static final Ctx.Entry<HttpResponse> NettyResponse = new Ctx.Entry<>("NettyResponse");
        public static final Ctx.Entry<ByteBuf> In = new Ctx.Entry<>("In");
        public static final Ctx.Entry<ByteBuf> Out = new Ctx.Entry<>("Out");
        public static final Ctx.Entry<HttpHeaders> TrailingHeaders = new Ctx.Entry<>("TrailingHeaders");
        public static final Ctx.Entry<Map<String, List<String>>> Queries = new Ctx.Entry<>("Queries");
        public static final Ctx.Entry<Map<String, String>> InUrlParams = new Ctx.Entry<>("InUrlParams");
        public static final Ctx.Entry<HttpPostRequestDecoder> FromData = new Ctx.Entry<>("FormData");
        public static final Ctx.Entry<CookieDecoder> Cookies = new Ctx.Entry<>("Cookies");
    }

    default CtxHttp addHandlers(CtxHandler... handlers){
        Executable[] execs = S._for(handlers).map(
            h -> Executable.of("h@"+h.hashCode(), c-> h.apply((CtxHttp)c))
        ).joinArray(new Executable[handlers.length]);

        this.push(execs);
        return this;
    }

    default CtxHttp addHandlers(Iterable<CtxHandler> handlers){
        this.push(S._for(handlers).map(
            h -> Executable.of("h@"+h.hashCode(), c-> h.apply((CtxHttp)c))
        ));
        return this;
    }

    interface Headers extends CtxHttp {

        default String header(AsciiString key){
            HttpHeaders reqHeaders = this.get(Keys.NettyRequest).headers();
            HttpHeaders trailingHeaders = this.get(Keys.TrailingHeaders);
            String ret = reqHeaders.get(key);
            if(ret == null) ret = trailingHeaders.get(key);
            return ret;
        }

        default Map<String, List<String>> all(){
            HttpHeaders reqHeaders = this.get(Keys.NettyRequest).headers();
            HttpHeaders trailingHeaders = this.get(Keys.TrailingHeaders);
            Map<String, List<String>> ret = new HashMap<>();
            S._for(reqHeaders.names()).each(name -> {
                ret.put(name, reqHeaders.getAll(name));
            });
            S._for(trailingHeaders.names()).each(name -> {
                ret.put(name, reqHeaders.getAll(name));
            });
            return ret;
        }


    }

    interface Cookie extends CtxHttp {

    }

    interface FormData extends CtxHttp {

    }

    interface UploadFiles extends CtxHttp {

    }

    interface In extends CtxHttp {

    }

    interface Out extends CtxHttp {

    }

    interface Send extends CtxHttp {

        default void BadRequest(){
            this.nettyChannelHandlerContext()
                .writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST))
                .addListener(ChannelFutureListener.CLOSE);
        }

        default void ok() {
            this.nettyChannelHandlerContext()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK))
                .addListener(ChannelFutureListener.CLOSE);
        }
    }

}

