package pond.web.http;

import io.netty.buffer.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.STRING;
import pond.common.f.Either;
import pond.common.f.Tuple;
import pond.core.Context;
import pond.core.Ctx;
import pond.core.Executable;
import pond.net.CtxNet;
import pond.web.CtxHandler;
import pond.web.Request;
import pond.web.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public interface CtxHttp extends CtxNet {
    class Keys {
        public static final Ctx.Entry<HttpConfigBuilder> Config = new Ctx.Entry<>(CtxHttp.class,"Config");
        static final Ctx.Entry<HttpRequest> NettyRequest = new Ctx.Entry<>(CtxHttp.class,"NettyRequest");
        // public static final Ctx.Entry<HttpResponse> NettyResponse = new Ctx.Entry<>(CtxHttp.class, "NettyResponse");
        static final Ctx.Entry<ByteBuf> In = new Ctx.Entry<>(CtxHttp.class, "In");
        static final Ctx.Entry<ByteBuf> Out = new Ctx.Entry<>(CtxHttp.class, "Out");
        static final Ctx.Entry<HttpHeaders> TrailingHeaders = new Ctx.Entry<>(CtxHttp.class, "TrailingHeaders");
        static final Ctx.Entry<Map<String, List<String>>> Queries = new Ctx.Entry<>(CtxHttp.class,"Queries");
        static final Ctx.Entry<Map<String, String>> InUrlParams = new Ctx.Entry<>(CtxHttp.class, "InUrlParams");

        // static final Ctx.Entry<HttpPostRequestDecoder> PostRequestDecoder = new Ctx.Entry<>(CtxHttp.class, "PostRequestDecoder");

        static final Ctx.Entry<Request> Request = new Ctx.Entry<>(CtxHttp.class, "Request");
        static final Ctx.Entry<Request> Response = new Ctx.Entry<>(CtxHttp.class, "Response");

        static final Ctx.Entry<Boolean> HasCookie = new Ctx.Entry<>(CtxHttp.class,"HasCookie");
        static final Ctx.Entry<Set<Cookie>> Cookies = new Ctx.Entry<>(CtxHttp.class,"Cookies");

        static final Ctx.Entry<Tuple<Map<String, List<String>>, List<FileUpload>>> FormData
            = new Ctx.Entry<>(CtxHttp.class, "FormData");
    }

    static ByteBuf str(String string){
        return Unpooled.wrappedBuffer(string.getBytes(CharsetUtil.UTF_8));
    }
    /**
     * Short hand for delegate
     * @return
     */
    default Context bind(){
       return delegate();
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

    default ByteBufInputStream in() {
        return new ByteBufInputStream(this.get(Keys.In));
    }

    default ByteBufOutputStream out(){
        return new ByteBufOutputStream(this.get(Keys.Out));
    }

    interface Headers extends CtxHttp {

        default Tuple<HttpHeaders, HttpHeaders> headerAndTrailing(){
            return Tuple.pair(this.get(Keys.NettyRequest).headers(), this.get(Keys.TrailingHeaders));
        }

        default String header(AsciiString key){
            var reqHeaders = this.get(Keys.NettyRequest).headers();
            var trailingHeaders = this.get(Keys.TrailingHeaders);
            String ret = reqHeaders.get(key);
            if(ret == null) ret = trailingHeaders.get(key);
            return ret;
        }

        default Map<String, List<String>> all(){

            var config = this.get(Keys.Config);
            var is_case_sensitive = config.isHeaderCaseSensitive();
            HttpHeaders reqHeaders = this.get(Keys.NettyRequest).headers();
            HttpHeaders trailingHeaders = this.get(Keys.TrailingHeaders);
            Map<String, List<String>> ret = new HashMap<>();
            S._for(reqHeaders.names()).each(name -> {
                ret.put(name, is_case_sensitive
                                  ? reqHeaders.getAll(name)
                                  : S._for(reqHeaders.getAll(name)).map(String::toLowerCase).toList()
                );
            });
            S._for(trailingHeaders.names()).each(name -> {
                ret.put(name, is_case_sensitive
                    ? reqHeaders.getAll(name)
                    : S._for(reqHeaders.getAll(name)).map(String::toLowerCase).toList()
                );
            });
            return ret;
        }

        default String ContentType() {
            //content-type exists in header only
            var tuple = headerAndTrailing();
            return tuple._a.get(HttpHeaderNames.CONTENT_TYPE);
        }

    }

    interface Send extends CtxHttp {


        default void BadRequest(){
            this.nettyChannelHandlerContext()
                .writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST))
                .addListener(ChannelFutureListener.CLOSE);
        }

        default void Ok(ByteBuf buf) {
            this.nettyChannelHandlerContext()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
        }

        default void NotFound(ByteBuf buf) {
            this.nettyChannelHandlerContext()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NOT_FOUND,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
        }

        default void InternalServerError(ByteBuf buf) {
            this.nettyChannelHandlerContext()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
        }
    }

    interface Cookies extends CtxHttp {
        default Set<Cookie> cookies() {
            return this.get(Keys.Cookies);
        }

        default Cookie cookie(String name) {
            if(name == null) return null;
            return S._for(cookies()).filter(c -> name.equals(c.name())).first();
        }

    }

    /**
     * Represents application/x-www-form-urlencoded or multipart/form-data
     * @html https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1
     *
     */
    interface FormData extends CtxHttp {

        default List<String> params(String name) {
            return params().get(name);
        }


        default Map<String, List<String>> params(){
            return this.get(Keys.FormData)._a;
        }

        default String param(String name) {
            return S._for(S._try_get(params(name), Collections.emptyList())).first();
        }

        default List<FileUpload> files() {
            return this.get(Keys.FormData)._b;
        }

        default List<FileUpload> files(String name) {
            return S._for(files()).filter(x -> x.getName() != null && x.getName().equals(name)).toList();
        }

        default FileUpload file(String name) {
            return S._for(files(name)).first();
        }

    }

}

