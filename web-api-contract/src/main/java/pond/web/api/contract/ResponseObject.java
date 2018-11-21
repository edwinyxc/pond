package pond.web.api.contract;

import io.netty.handler.codec.http.HttpHeaderNames;
import pond.common.S;
import pond.core.Ctx;
import pond.core.CtxHandler;
import pond.web.http.HttpCtx;
import pond.web.router.RouterCtx;

import java.lang.annotation.Annotation;

public class ResponseObject<T> {

    public final int code;

    public final String description;

    public final Type<?, T> type;

    //public final CtxHandler consumer;

    public ResponseObject(int code, String description, Type<Object, T> type) {
        this.code = code;
        this.description = description;
        assert type != null;
        this.type = type;


//        this.consumer = http -> {
//                var ctx = (RouterCtx & HttpCtx.Send) http::bind;
//                var headers = ctx.response().headers();
//                var contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
//                S._for(produces).each(outgoingContentType -> {
//                    if (contentType == null || contentType.contains(outgoingContentType)) {
//                        headers.add(HttpHeaderNames.CONTENT_TYPE, outgoingContentType);
//                    }
//                });
//                var result = http.get(Ctx.LAST_RESULT);
//                Object finalResult = (type != null)? type.consume(result) : result;
//                S.echo("Result ==> ", result);
//                ctx.send(result);
//            };
    }


}

