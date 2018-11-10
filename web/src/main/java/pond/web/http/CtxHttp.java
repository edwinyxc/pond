package pond.web.http;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.STRING;
import pond.common.f.Callback;
import pond.common.f.Tuple;
import pond.core.Context;
import pond.core.Ctx;
import pond.core.Executable;
import pond.net.CtxNet;
import pond.net.NetServer;
import pond.web.CtxHandler;
import pond.web.Request;
import pond.web.Response;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

public interface CtxHttp extends CtxNet {
    class Keys {
        public static final Entry<HttpConfigBuilder> Config = new Entry<>(CtxHttp.class, "Config");
        static final Entry<HttpRequest> NettyRequest = new Ctx.Entry<>(CtxHttp.class, "NettyRequest");
        //static final Entry<HttpResponse> NettyResponse = new Ctx.Entry<>(CtxHttp.class, "NettyResponse");
        // public static final Ctx.Entry<HttpResponse> NettyResponse = new Ctx.Entry<>(CtxHttp.class, "NettyResponse");
        static final Entry<ByteBuf> In = new Ctx.Entry<>(CtxHttp.class, "In");
        static final Entry<ByteBuf> Out = new Ctx.Entry<>(CtxHttp.class, "Out");
        static final Entry<HttpHeaders> TrailingHeaders = new Ctx.Entry<>(CtxHttp.class, "TrailingHeaders");
        static final Entry<Map<String, List<String>>> Queries = new Ctx.Entry<>(CtxHttp.class, "Queries");
        static final Entry<Map<String, List<String>>> InUrlParams = new Ctx.Entry<>(CtxHttp.class, "InUrlParams");

        // static final Ctx.Entry<HttpPostRequestDecoder> PostRequestDecoder = new Ctx.Entry<>(CtxHttp.class, "PostRequestDecoder");
//        static final Ctx.Entry<Request> Request = new Ctx.Entry<>(CtxHttp.class, "Request");
//        static final Ctx.Entry<Request> Response = new Ctx.Entry<>(CtxHttp.class, "Response");

//        static final Ctx.Entry<Boolean> HasCookie = new Ctx.Entry<>(CtxHttp.class,"HasCookie");
        static final Ctx.Entry<Set<Cookie>> Cookies = new Ctx.Entry<>(CtxHttp.class,"Cookies");

        static final Ctx.Entry<Body.FormData> FormData = new Ctx.Entry<>(CtxHttp.class, "FormData");
        static final Ctx.Entry<Send.ResponseBuilder> ResponseBuilder =
            new Ctx.Entry<>(CtxHttp.class, "ResponseBuilder");


        //static final Entry<Send.ResponseBuilder> RequestBuilder = new Entry<>(CtxHttp.class, "RequestBuilder");
    }

    static ByteBuf str(String string) {
        return Unpooled.wrappedBuffer(string.getBytes(CharsetUtil.UTF_8));
    }

    default String method() {
        return this.get(Keys.NettyRequest).method().toString();
    }

    default String uri() {
        return this.get(Keys.NettyRequest).uri();
    }

    default String path() {
        return S._try_ret(() -> new URI(uri()).getPath());
    }

    default Send.ResponseBuilder response(HttpResponseStatus status) {
        Send.ResponseBuilder builder = this.get(Keys.ResponseBuilder);
        if(builder == null)
        {
            builder = new Send.ResponseBuilder(this, status);
            this.set(Keys.ResponseBuilder, builder);
        }
        return builder;
    }

    default Send.ResponseBuilder response() {
        return response(HttpResponseStatus.OK);
    }

    /*
    default Send.ResponseBuilder response(int code) {
        return response(HttpResponseStatus.valueOf(code));
    }
    */
//    default ByteBufInputStream in() {
//        return new ByteBufInputStream(this.get(Keys.In));
//    }
//
//    default ByteBufOutputStream out() {
//        return new ByteBufOutputStream(this.get(Keys.Out));
//    }

    interface Headers extends CtxHttp {

        default Tuple<HttpHeaders, HttpHeaders> headerAndTrailing() {
            return Tuple.pair(this.get(Keys.NettyRequest).headers(), this.get(Keys.TrailingHeaders));
        }

        default String header(String key) {
            var reqHeaders = this.get(Keys.NettyRequest).headers();
            var trailingHeaders = this.get(Keys.TrailingHeaders);
            String ret = reqHeaders.get(key);
            if (ret == null) ret = trailingHeaders.get(key);
            return ret;
        }


        default Map<String, List<String>> all() {

            var config = this.get(Keys.Config);
            var is_case_sensitive = config.isHeaderCaseSensitive();
            HttpHeaders reqHeaders = this.get(Keys.NettyRequest).headers();
            HttpHeaders trailingHeaders = this.get(Keys.TrailingHeaders);
            Map<String, List<String>> ret = new HashMap<>();
            S._for(reqHeaders.names()).each(name -> {
                ret.put(name, is_case_sensitive
                                  ? reqHeaders.getAll(name)
                                  : S._for(reqHeaders.getAll(name)).map(i -> i.toLowerCase()).toList()
                );
            });
            S._for(trailingHeaders.names()).each(name -> {
                ret.put(name, is_case_sensitive
                                  ? reqHeaders.getAll(name)
                                  : S._for(reqHeaders.getAll(name)).map(i -> i.toLowerCase()).toList()
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

    interface Queries extends CtxHttp {
        default Map<String,  List<String>> inUrlParams() {
            return getLazy(Keys.InUrlParams, new LinkedHashMap<String, List<String>>());
        }

        default List<String> params(String name) {
            return params().get(name);
        }

        default Map<String, List<String>> params() {
            return this.get(Keys.Queries);
        }

        default String param(String name) {
            return S._for(S._try_get(params(name), Collections.emptyList())).first();
        }

    }

    interface Body extends CtxHttp {

        class FormData extends Tuple<Map<String, List<String>>, Map<String, List<FileUpload>>> implements AutoCloseable {
            final HttpPostRequestDecoder decoder;

            protected FormData(HttpPostRequestDecoder decoder) {
                super(new LinkedHashMap<>(), new LinkedHashMap<>());
                this.decoder = decoder;
            }

            public Map<String, List<String>> params() {
                return _a;
            }

            public Map<String, List<FileUpload>> files() {
                return _b;
            }

            public void offer(HttpContent content) throws IOException {
                decoder.offer(content);
                try {
                    for (InterfaceHttpData data = decoder.next();
                         data != null && decoder.hasNext();
                         data = decoder.next()) {
                        InterfaceHttpData.HttpDataType type = data.getHttpDataType();
                        switch (type) {
                            case Attribute: {
                                Attribute attr = (Attribute) data;

                                S._debug(NetServer.logger, log -> {
                                    try {
                                        log.debug("PARSE ATTR: " + attr.getName() + " : " + attr.getValue());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                appendToMap(params(), attr.getName(), attr.getValue());
                                break;
                            }
                            case FileUpload: {
                                FileUpload fileUpload = (FileUpload) data;
                                S._debug(NetServer.logger, log -> {
                                    try {
                                        log.debug("PARSE FILE: " + fileUpload.getName()
                                                      + " : " + fileUpload.getFilename()
                                                      + " : " + fileUpload.getFile().getAbsolutePath());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                appendToMap(files(), fileUpload.getName(), fileUpload);
                                break;
                            }
                        }
                    }

                } catch (HttpPostRequestDecoder.EndOfDataDecoderException ignore) {
                }
            }

            @Override
            public void close() {
                decoder.cleanFiles();
                decoder.destroy();
            }
        }

        default ByteBufInputStream inputStream() {
            return new ByteBufInputStream(this.get(Keys.In));
        }

        default ByteBuf raw() {
            return this.get(Keys.In);
        }

        default String asString(Charset charset) {
            return raw().toString(charset);
        }

        default boolean isMultipart(){
            return HttpPostRequestDecoder.isMultipart(this.get(Keys.NettyRequest));
        }

        default FormData multipart() throws IllegalAccessException {
            if (isMultipart()) {
                return this.get(Keys.FormData);
            } else throw new IllegalAccessException("Request is not a multipart request");
        }

        default Map<String, List<String>> params (Charset charset) {
            return new QueryStringDecoder(asString(charset)).parameters();
        }

        default Map<String, List<String>> params () {
            return new QueryStringDecoder(asString(CharsetUtil.UTF_8)).parameters();
        }

    }

    interface Send extends CtxHttp {

        class ResponseBuilder {
            CtxHttp http;
            ByteBuf buf;
            FullHttpResponse httpResponse;
            HttpRequest httpRequest;

            private ResponseBuilder(CtxHttp http, HttpResponseStatus status) {
                this.http = http;
                httpRequest = http.get(Keys.NettyRequest);
                buf = http.get(Keys.Out);
                if (buf == null || buf.writableBytes() == 0) {
                    buf = PooledByteBufAllocator.DEFAULT.heapBuffer();
                }
                httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf, false);
            }

            public HttpHeaders headers() {
                return httpResponse.headers();
            }

            public ResponseBuilder header(String k, String v) {
                headers().set(k, v);
                return this;
            }

            public ResponseBuilder writeRaw(Callback<ByteBuf> cb) {
                cb.apply(buf);
                return this;
            }

            public ResponseBuilder writeStream(Callback<ByteBufOutputStream> cb) throws IOException {
                try (ByteBufOutputStream out = new ByteBufOutputStream(buf)) {
                    cb.apply(out);
                }
                return this;
            }

            public ResponseBuilder write(String string) {
                buf.writeCharSequence(string, Charset.defaultCharset());
                return this;
            }

            public ResponseBuilder write(String string, Charset charset) {
                buf.writeCharSequence(string, charset);
                return this;
            }

            public ByteBufOutputStream outputStream() {
                return new ByteBufOutputStream(buf);
            }

            public FullHttpResponse build() {
                //validate keepAlive
                if (HttpUtil.isKeepAlive(httpRequest)) {
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

                    if (httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH) == null) {
                        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
                    }
                } else {
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                }
                //remove from CtxHttp, if user called this, we should not add last Flow to the NetServer's flow
                this.http.set(Keys.ResponseBuilder, null);

                return httpResponse;
            }
        }

        /**
         * sync-send
         * @param response
         * @return
         */
        default ChannelFuture send(FullHttpResponse response) {
            ChannelFuture future = chctx().writeAndFlush(response);
            terminate();
            if (HttpUtil.isKeepAlive(this.get(Keys.NettyRequest))) {
                return future;
            } else {
                return future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        default ChannelFuture sendFile(File file) throws IOException {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpUtil.setContentLength(response, raf.length());
            if (response.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
                String filename = file.getName();
                int dot_pos = filename.lastIndexOf(".");
                if (dot_pos != -1 && dot_pos < filename.length() - 1) {
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                        MimeTypes.getMimeType(filename.substring(dot_pos + 1)));
                } else {
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                        (MimeTypes.MIME_APPLICATION_OCTET_STREAM));
                }
            }
            this.chctx().write(response);
            ChannelFuture sendFileFuture = this.chctx().write(
                new DefaultFileRegion(raf.getChannel(), 0, raf.length())
                , this.chctx().newProgressivePromise());
            //sendFileFuture = ctx.write(new HttpChunkedInput(new ChunkedFile(raf, offset, length, 65536)),
              //      ctx.newProgressivePromise());

            sendFileFuture.addListener(
                new ChannelProgressiveFutureListener() {
                    @Override
                    public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                        if (total < 0) { // total unknown
                            S._debug(
                                NetServer.logger,
                                logger -> logger.debug(future.channel() + " Transfer progress: " + progress));
                        } else {
                            S._debug(
                                NetServer.logger,
                                logger -> logger.debug(future.channel() + " Transfer progress: " + progress + " / " + total));
                        }
                    }

                    @Override
                    public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                        S._debug(
                            NetServer.logger,
                            logger -> logger.debug(future.channel() + " Transfer complete."));
                    }
                }
            );

            ChannelFuture lastFuture =
                this.chctx().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

            if(!HttpUtil.isKeepAlive(this.get(Keys.NettyRequest))) {
                lastFuture.addListener(ChannelFutureListener.CLOSE);
            }
            terminate();
            return lastFuture;
            // HttpChunkedInput will pipe the end marker (LastHttpContent) for us.
        }


//        default ChannelFuture write(
//            HttpResponseStatus status, ByteBuf content,
//            HttpHeaders headers, HttpHeaders trailingHeaders) {
//            return this.chctx().write(new DefaultFullHttpResponse(
//                HttpVersion.HTTP_1_1,
//                status,
//                content,
//                headers,
//                trailingHeaders
//            ));
//        }


        /*
        Internal Short-hands
         */
        default void BadRequest() {
            this.chctx()
                .writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST))
                .addListener(ChannelFutureListener.CLOSE);

            terminate();
        }

        default void Ok(ByteBuf buf) {
            this.chctx()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
            terminate();
        }

        default void NotFound(ByteBuf buf) {
            this.chctx()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NOT_FOUND,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
            terminate();
        }

        default void InternalServerError(ByteBuf buf) {
            this.chctx()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
            terminate();
        }
    }


    static <E> void appendToMap(Map<String, List<E>> map, String name, E value) {
        List<E> values;
        if ((values = map.get(name)) != null) values.add(value);
        else map.put(name, S._tap(new ArrayList<E>(), list -> list.add(value)));
    }

    public static <E> void appendToMap(Map<String, List<E>> map, String name, List<E> values) {
        for (E val : values) {
            appendToMap(map, name, val);
        }
    }

    interface Cookies extends CtxHttp {
        default Set<Cookie> cookies() {
            if(this.get(Keys.Cookies) == null){
                var httpRequest = this.get(Keys.NettyRequest);
                var trailingHeaders = this.get(Keys.TrailingHeaders);
                //cookies
                //merge into one
                var cookie = httpRequest.headers().get(HttpHeaderNames.COOKIE);
                if(STRING.notBlank(cookie)){
                    cookie += S.avoidNull(trailingHeaders.get(HttpHeaderNames.COOKIE), "");
                }

                if(STRING.notBlank(cookie)){
                    var cookies = ServerCookieDecoder.STRICT.decode(cookie);
                    this.set(Keys.Cookies, cookies);
                }else {
                    this.set(Keys.Cookies, Collections.emptySet());
                }
            }
            return this.get(Keys.Cookies);
        }

        default Cookie cookie(String name) {
            if (name == null) return null;
            return S._for(cookies()).filter(c -> name.equals(c.name())).first();
        }

    }

    interface Lazy extends CtxHttp {
        default Request req(){
            return () -> this;
        }

        default Response resp(){
            return () -> this;
        }

    }

}

