package pond.web.http;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import pond.common.JSON;
import pond.common.S;
import pond.common.STREAM;
import pond.common.STRING;
import pond.common.f.Callback;
import pond.common.f.Tuple;
import pond.core.Ctx;
import pond.core.Entry;
import pond.net.CtxNet;
import pond.net.NetServer;
import pond.web.EndToEndException;
import pond.web.Request;
import pond.web.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

public interface HttpCtx extends CtxNet {
    Entry<HttpConfigBuilder> CONFIG = new Entry<>(HttpCtx.class, "CONFIG");
    Entry<HttpRequest> NETTY_REQUEST = new Entry<>(HttpCtx.class, "NETTY_REQUEST");
    //static final Entry<HttpResponse> NettyResponse = new Ctx.Entry<>(HttpCtx.class, "NettyResponse");
    // public static final Ctx.Entry<HttpResponse> NettyResponse = new Ctx.Entry<>(HttpCtx.class, "NettyResponse");
    Entry<ByteBuf> IN = new Entry<>(HttpCtx.class, "IN");
    Entry<ByteBuf> OUT = new Entry<>(HttpCtx.class, "OUT");
    Entry<HttpHeaders> TRAILING_HEADERS = new Entry<>(HttpCtx.class, "TRAILING_HEADERS");

    Entry<String> PATH = new Entry<>(HttpCtx.class, "PATH");
    Entry<String> URI = new Entry<>(HttpCtx.class, "URI");

    Entry<Map<String, List<String>>> Queries = new Entry<>(HttpCtx.class, "Queries");
    Entry<Map<String, List<String>>> IN_URL_PARAMS = new Entry<>(HttpCtx.class, "IN_URL_PARAMS");
    Entry<Map<String, List<String>>> HEADERS = new Entry<>(HttpCtx.class, "HEADERS");
    Entry<Set<Cookie>> COOKIES = new Entry<>(HttpCtx.class, "COOKIES");
    Entry<Body.FormData> FORM_DATA = new Entry<>(HttpCtx.class, "FORM_DATA");
    Entry<Send.ResponseBuilder> RESPONSE_BUILDER = new Entry<>(HttpCtx.class, "RESPONSE_BUILDER");

//
    Entry<Request> REQ = new Entry<>(HttpCtx.class, "REQ");
    Entry<Response> RESP = new Entry<>(HttpCtx.class, "RESP");


        // static final Ctx.Entry<HttpPostRequestDecoder> PostRequestDecoder = new Ctx.Entry<>(HttpCtx.class, "PostRequestDecoder");
//        static final Ctx.Entry<Request> Request = new Ctx.Entry<>(HttpCtx.class, "Request");
//        static final Ctx.Entry<Request> Response = new Ctx.Entry<>(HttpCtx.class, "Response");


        //static final Entry<Send.RESPONSE_BUILDER> RequestBuilder = new Entry<>(HttpCtx.class, "RequestBuilder");

    static ByteBuf str(String string) {
        return Unpooled.wrappedBuffer(string.getBytes(CharsetUtil.UTF_8));
    }

    default String method() {
        return this.get(NETTY_REQUEST).method().toString();
    }

    default String uri() {
        return this.getOrPutDefault(URI, this.get(NETTY_REQUEST).uri());
    }

    default String path() {
        return this.getOrPutDefault(PATH, S._try_ret(() -> new URI(uri()).getPath()));
    }

    default Send.ResponseBuilder response(HttpResponseStatus status) {
        Send.ResponseBuilder builder = this.get(RESPONSE_BUILDER);
        if (builder == null) {
            builder = new Send.ResponseBuilder(this, status);
            this.set(RESPONSE_BUILDER, builder);
        }
        return builder;
    }

    default HttpRequest request() {
        return this.get(NETTY_REQUEST);
    }

    default Send.ResponseBuilder response() {
        return response(HttpResponseStatus.OK);
    }

    default Send.ResponseBuilder response(int code) {
        return response(HttpResponseStatus.valueOf(code));
    }

    default <T> void result(Callback.C2<HttpCtx, T> result, T value) {
        result.apply(this, value);
    }

    default void result(Callback.C2<HttpCtx, Void> result) {
        result.apply(this, null);
    }



    /*
    default Send.RESPONSE_BUILDER response(int code) {
        return response(HttpResponseStatus.valueOf(code));
    }
    */
//    default ByteBufInputStream in() {
//        return new ByteBufInputStream(this.get(Keys.IN));
//    }
//
//    default ByteBufOutputStream out() {
//        return new ByteBufOutputStream(this.get(Keys.OUT));
//    }

    interface Headers extends HttpCtx {

        default Tuple<HttpHeaders, HttpHeaders> headerAndTrailing() {
            return Tuple.pair(this.get(NETTY_REQUEST).headers(), this.get(TRAILING_HEADERS));
        }

        default String header(String key) {
            var reqHeaders = this.get(NETTY_REQUEST).headers();
            var trailingHeaders = this.get(TRAILING_HEADERS);
            String ret = reqHeaders.get(key);
            if (ret == null) ret = trailingHeaders.get(key);
            return ret;
        }


        default Map<String, List<String>> headers() {
            return this.getOrSupplyDefault(HEADERS, () -> {
                var config = this.get(CONFIG);
                var is_case_sensitive = config.isHeaderCaseSensitive();
                HttpHeaders reqHeaders = this.get(NETTY_REQUEST).headers();
                HttpHeaders trailingHeaders = this.get(TRAILING_HEADERS);
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
            });
        }

        default String ContentType() {
            //content-type exists in header only
            var tuple = headerAndTrailing();
            return tuple._a.get(HttpHeaderNames.CONTENT_TYPE);
        }

    }

    interface Queries extends HttpCtx {
        default Map<String, List<String>> inUrlParams() {
            return getOrPutDefault(IN_URL_PARAMS, new LinkedHashMap<>());
        }

        default List<String> queries(String name) {
            return queries().get(name);
        }

        default Map<String, List<String>> queries() {
            return this.get(Queries);
        }

        default String query(String name) {
            return S._for(S._try_get(queries(name), Collections.emptyList())).first();
        }

    }

    interface Body extends HttpCtx {

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

        default ByteBufInputStream bodyAsInputStream() {
            return new ByteBufInputStream(this.get(IN));
        }

        default ByteBuf bodyAsRaw() {
            return this.get(IN);
        }

        default String bodyAsString(Charset charset) {
            return bodyAsRaw().toString(charset);
        }

        default boolean bodyIsMultipart() {
            return HttpPostRequestDecoder.isMultipart(this.get(NETTY_REQUEST));
        }

        default FormData bodyAsMultipart() throws IllegalAccessException {
            if (bodyIsMultipart()) {
                return this.get(FORM_DATA);
            } else throw new IllegalAccessException("Request is not a bodyAsMultipart request");
        }

        default Map<String,Object> bodyAsJson() {
            //TODO refine
            return JSON.parse(bodyAsString(CharsetUtil.UTF_8));
        }

        default Map<String, List<String>> params(Charset charset) {
            return new QueryStringDecoder(bodyAsString(charset)).parameters();
        }

        default Map<String, List<String>> params() {
            return new QueryStringDecoder(bodyAsString(CharsetUtil.UTF_8)).parameters();
        }

    }

    interface Send extends HttpCtx {

        class ResponseBuilder {
            HttpCtx http;
            ByteBuf buf;
            FullHttpResponse httpResponse;
            HttpRequest httpRequest;

            private ResponseBuilder(HttpCtx http, HttpResponseStatus status) {
                this.http = http;
                httpRequest = http.get(NETTY_REQUEST);
                buf = http.get(OUT);
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
                buf.writeCharSequence(string, CharsetUtil.UTF_8);
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
                //remove from HttpCtx, if user called this, we should not add last Flow to the NetServer's flow
                this.http.set(RESPONSE_BUILDER, null);

                return httpResponse;
            }
        }

        default Send contentType(String contentType) {
            contentType(contentType, false);
            return this;
        }

        default Send contentType(String contentType, boolean override){
            if(!override) {
                if(this.response().headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
                    this.response().headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
                }
            }else {
                this.response().headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
            }
            return this;
        }


        default void send(Object t){
            var ctx = this;
            if (t == null) t = "";
            if (t instanceof FullHttpResponse
                || t instanceof ByteBuf
                || t instanceof byte[]
                || t instanceof Byte[]
            ) {
                chctx().writeAndFlush(t);
                terminate();
            } else if (t instanceof CharSequence) {
                ctx.response().write((String) t);
                contentType(MIME.MIME_TEXT_PLAIN);
            } else if (t instanceof File) {
                try {
                    ctx.sendFile((File) t);
                } catch (IOException e) {
                    throw new EndToEndException(500, e.getMessage(), e);
                }
            } else if (t instanceof InputStream) {
                var inputStream = (InputStream) t;
                try {
                    contentType(MIME.MIME_APPLICATION_OCTET_STREAM);
                    STREAM.pipe(inputStream, ctx.response().outputStream());
                } catch (IOException e) {
                    throw new EndToEndException(500, e.getMessage(), e);
                }
            } else {
                //TODO
                //handler DEFAULT json serialization
                S.echo("Default send", t.getClass(), t);
                contentType(MIME.MIME_APPLICATION_JSON);
                ctx.response().write(JSON.stringify(t));
            }
        }

        private ChannelFuture sendFile(File file) throws IOException {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpUtil.setContentLength(response, raf.length());
            if (response.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
                String filename = file.getName();
                int dot_pos = filename.lastIndexOf(".");
                if (dot_pos != -1 && dot_pos < filename.length() - 1) {
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                        MIME.getMimeType(filename.substring(dot_pos + 1)));
                } else {
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                        (MIME.MIME_APPLICATION_OCTET_STREAM));
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

            if (!HttpUtil.isKeepAlive(this.get(NETTY_REQUEST))) {
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
        default void sendBadRequest() {
            this.chctx()
                .writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST))
                .addListener(ChannelFutureListener.CLOSE);

            terminate();
        }

        default void sendOk(ByteBuf buf) {
            this.chctx()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
            terminate();
        }

        default void sendNotFound(ByteBuf buf) {
            this.chctx()
                .writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NOT_FOUND,
                    buf
                ))
                .addListener(ChannelFutureListener.CLOSE);
            terminate();
        }

        default void sendInternalServerError(ByteBuf buf) {
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

    static <E> void appendToMap(Map<String, List<E>> map, String name, List<E> values) {
        for (E val : values) {
            appendToMap(map, name, val);
        }
    }

    interface Cookies extends HttpCtx {
        default Set<Cookie> cookies() {
            if (this.get(COOKIES) == null) {
                var httpRequest = this.get(NETTY_REQUEST);
                var trailingHeaders = this.get(TRAILING_HEADERS);
                //cookies
                //merge into one
                var cookie = httpRequest.headers().get(HttpHeaderNames.COOKIE);
                if (STRING.notBlank(cookie)) {
                    cookie += S.avoidNull(trailingHeaders.get(HttpHeaderNames.COOKIE), "");
                }

                if (STRING.notBlank(cookie)) {
                    var cookies = ServerCookieDecoder.STRICT.decode(cookie);
                    this.set(COOKIES, cookies);
                } else {
                    this.set(COOKIES, Collections.emptySet());
                }
            }
            return this.get(COOKIES);
        }

        default Cookie cookie(String name) {
            if (name == null) return null;
            return S._for(cookies()).filter(c -> name.equals(c.name())).first();
        }

        default Cookies addCookie(Cookie cookie) {
            this.response().headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            return this;
        }

        default Cookies removeCookie(Cookie cookie){
            if(cookie != null){
                cookie.setValue(null);
                cookie.setMaxAge(-1);
                this.response().headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
            return this;
        }
    }

    interface Lazy extends HttpCtx {
        default Request req() {
            return () -> this;
        }

        default Response resp() {
            return () -> this;
        }

    }


}

