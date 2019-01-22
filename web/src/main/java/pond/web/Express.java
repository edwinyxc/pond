package pond.web;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import pond.common.f.Function;
import pond.core.CtxHandler;
import pond.web.http.HttpCtx;


public interface Express<CTX extends HttpCtx> extends CtxHandler<CTX> {



    static CtxHandler<HttpCtx> express(C2<Request, Response> c){
        return ctx -> {
            HttpCtx.Lazy lazy = ctx::bind;
            c.apply(lazy.req(), lazy.resp());
        };
    }

    static <T> CtxHandler<HttpCtx> func(Function.F2<T, Request, Response> f) {
        return http -> {
            var ctx = (HttpCtx.Send & HttpCtx.Lazy) http::bind;
            try {
                Object ret = f.apply(ctx.req(), ctx.resp());
                ctx.send(ret);
            } catch (EndToEndException e){
                ctx.send(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(e.http_status),
                    Unpooled.wrappedBuffer(e.message.getBytes(CharsetUtil.UTF_8))
                    )
                );
            } catch (Exception e){
                e.printStackTrace();
            }
        };
    }



}
