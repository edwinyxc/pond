package pond.web.restful;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import pond.common.JSON;
import pond.common.S;
import pond.common.STREAM;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.core.Ctx;
import pond.web.*;
import pond.web.http.HttpCtx;
import pond.web.http.MIME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;


/**
 * Created by ed on 3/8/17.
 */
public class ResultDef<T> implements Callback.C2<HttpCtx, T> {
    final Callback.C2<HttpCtx, T> handler;
    final Integer httpStatusCode;
    final String description;
    final Set<String> produces;

    Schema schema = Schema.STRING();
    final Set<String> headers;

    private ResultDef(Integer code, String desc, Callback.C2<HttpCtx, T> handler) {
        this.headers = new HashSet<>();
        this.produces = S._tap(new HashSet<>(), s -> {
            s.add(MIME.MIME_TEXT_PLAIN);
        });
        this.httpStatusCode = code;
        this.handler = handler;
        this.description = desc;
    }

    public ResultDef<T> schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public static <T> ResultDef<T> any(Integer code, String desc, Callback.C2<HttpCtx, T> cb) {
        return new ResultDef<T>(code, desc, cb);
    }

    public ResultDef<T> produces(String... produces) {
        this.produces.addAll(Arrays.asList(produces));
        return this;
    }

    public ResultDef<T> produces(Iterable<String> produces) {
        S._for(produces).each(this.produces::add);
        return this;
    }

    public static <A, T> ResultDef<A> adapt(Function<T, A> paramAdaptor, ResultDef<T> origin) {
        return new ResultDef<A>(origin.httpStatusCode, origin.description,
                (ctx, a) -> {
                    origin.handler.apply(ctx, paramAdaptor.apply(a));
                }).produces(origin.produces);
    }

    public static <A> ResultDef<Tuple<String[], A>> header(String[] headers, ResultDef<A> finish) {
        ResultDef<Tuple<String[], A>> def = new ResultDef<>(finish.httpStatusCode,
                "set headers and then " + finish.description,
                (ctx, t) -> {
                    String[] values = t._a;
                    for (int i = 0, len = Math.min(values.length, headers.length);
                         i < len; i++) {
                        ((HttpCtx.Lazy) ctx).resp().header(headers[i], values[i]);
                    }
                    finish.handler.apply(ctx, t._b);
                });

        def.headers.addAll(Arrays.asList(headers));
        def.produces(MIME.MIME_TEXT_PLAIN);
        return def;
    }

    /**
     * send the ctx dump
     *
     * @return a lambda ignores any input
     */
    public static ResultDef<Void> debug() {
        ResultDef<Void> def = new ResultDef<>(200, "debug", (ctx, t) -> {
            ((HttpCtx.Lazy) ctx::bind).resp().send(200, ctx.toString());
        });
        def.produces(MIME.MIME_TEXT_PLAIN);
        return def;
    }

    /**
     * send as text/plain, it the input is null, send "ok"
     *
     * @param desc description of this result
     * @return a lambda takes a string value
     */
    public static ResultDef<String> text(String desc) {
        return new ResultDef<String>(200, desc, (ctx, t) -> {
            ctx.response().headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            ctx.response().write(S.avoidNull(t, "ok"));
        }).produces(MIME.MIME_TEXT_PLAIN);
    }

    public static ResultDef<String> html(String desc) {
        return new ResultDef<String>(200, desc, (ctx, t) -> {
            ctx.response().headers().set(HttpHeaderNames.CONTENT_TYPE, MIME.MIME_TEXT_HTML);
            ctx.response().write(t);
        }).produces(MIME.MIME_TEXT_HTML);
    }


    /**
     * @return
     */
    public static ResultDef<Void> ok() {
        return new ResultDef<Void>(200, "ok",  (ctx, t) -> {
            ctx.response().headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            ctx.response().write("ok");
        }).produces(MIME.MIME_TEXT_PLAIN);
    }

    /**
     * send desc as is (text/plain)
     *
     * @param desc description
     * @param code httpStatusCode
     * @return a lambda ignores its input and send the desc
     */
    public static ResultDef<Void> lazy(int code, String desc) {
        return new ResultDef<Void>(code, desc, (ctx, t) -> {
            ctx.response(HttpResponseStatus.valueOf(code)).write(desc);
        }).produces(MIME.MIME_TEXT_PLAIN);
    }

    /**
     * send as application/json, it the input is null, send "{}"
     *
     * @param desc description of this result
     * @return a lambda takes a Object value
     */
    public static <T> ResultDef<T> json(int code, String desc) {
        return new ResultDef<T>(code, desc, (ctx, t) -> {
            Response resp = ((HttpCtx.Lazy) ctx::bind).resp();
            resp.contentType(MIME.MIME_APPLICATION_JSON);
            resp.send(code, JSON.stringify(t));
        }).produces(MIME.MIME_TEXT_PLAIN, MIME.MIME_APPLICATION_JSON);
    }

    /**
     * send as application/json, it the input is null, send "{}"
     *
     * @param desc description of this result
     * @return a lambda takes a Object value
     */
    public static <T> ResultDef<T> json(int code, String desc, Schema schema) {
        return new ResultDef<T>(code, desc, (ctx, t) -> {
            Response resp = ((HttpCtx.Lazy) ctx::bind).resp();
            resp.contentType(MIME.MIME_APPLICATION_JSON);
            resp.send(code, JSON.stringify(t));
        }).produces(MIME.MIME_TEXT_PLAIN, MIME.MIME_APPLICATION_JSON)
                .schema(schema);
    }

    /**
     * send as application/json, it the input is null, send "{}"
     *
     * @param desc description of this result
     * @return a lambda takes a Object value
     */
    public static <T> ResultDef<T> json(String desc) {
        return new ResultDef<T>(200, desc, (ctx, t) -> {
            ctx.response(200).write(JSON.stringify(S.avoidNull(t, "{}")));
        }).produces(MIME.MIME_TEXT_PLAIN, MIME.MIME_APPLICATION_JSON);
    }

    /**
     * send as application/json
     *
     * @param desc description of this result
     * @return a lambda takes a Tuple (x-total-count:Integer, rows:T)
     */
    public static <T> ResultDef<Tuple<T, Integer>> page(String desc) {
        return ResultDef
                .<Tuple<T, Integer>, Tuple<String[], T>>
                        adapt(
                        tuple -> Tuple.pair(
                                new String[]{String.valueOf(tuple._b)},
                                tuple._a
                        ),
                        header(
                                new String[]{"X-Total-Count"},
                                ResultDef.<T>json(desc)
                        )
                ).produces(MIME.MIME_TEXT_PLAIN, MIME.MIME_APPLICATION_JSON);
    }

    /**
     * send file based at the file's ext
     *
     * @param desc description of this result
     * @return a lambda takes a File input
     */
    public static ResultDef<File> file(String desc) {
        return new ResultDef<File>(200, desc, (ctx, t) -> {

        }).produces("application/octet-stream");
    }

    public static ResultDef<Tuple<Class, String>> resourceAsFile(String desc) {
        return new ResultDef<Tuple<Class, String>>(200, desc, (ctx, t) -> {

            ClassLoader loader = t._a.getClassLoader();
            String filename = t._b;

            Response resp = ((HttpCtx.Lazy) ctx).resp();

            int dot_pos = filename.lastIndexOf(".");
            if (dot_pos != -1 && dot_pos < filename.length() - 1) {
                resp.contentType(MIME.getMimeType(filename.substring(dot_pos + 1)));
            } else {
                resp.contentType(MIME.MIME_TEXT_HTML);
            }
            try(InputStream in = loader.getResourceAsStream(filename)){
                STREAM.write(in, resp.out());
                resp.send(200);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).produces("text/html", "text/css", MIME.MIME_APPLICATION_JAVA_ARCHIVE);
    }


    /**
     * send download file as octet-stream with filename defined
     *
     * @param desc description of this result
     * @return a lambda takes a Tuple(filename:String, Callback(out:OutputStream))
     */
    public static ResultDef<Tuple<String, Callback<OutputStream>>> download(String desc) {
        return new ResultDef<Tuple<String, Callback<OutputStream>>>(200, desc, (ctx, t) -> {
            String filename = t._a;
            Callback<OutputStream> outputStreamCallback = t._b;
            Response resp = ((HttpCtx.Lazy) ctx::bind).resp();
            resp.header("Content-Disposition", String.format("attachment;filename=%s", filename));
            resp.contentType("application/octet-stream");
            outputStreamCallback.apply(resp.out());
            resp.send(200);
        }).produces("application/octet-stream");
    }

    //errors

    public static ResultDef<String> error(int code, String desc) {
        return new ResultDef<>(code, desc, (ctx, msg) -> {
            ((HttpCtx.Lazy) ctx).resp().sendError(code, msg);
        });
    }

    /**
     * send error as {code: _, msg: _}
     *
     * @param code httpStatusCode
     * @param desc
     * @return a lambda takes a Tuple(innerCode:Integer, msg:String)
     */
    public static ResultDef<Tuple<Integer, String>> errorJSON(int code, String desc) {
        return new ResultDef<Tuple<Integer, String>>(code, desc, (ctx, t) -> {
            int inner_code = t._a;
            String msg = t._b;
            ((HttpCtx.Lazy) ctx::bind).resp().sendError(code, JSON.stringify(new HashMap<String, Object>() {{
                put("code", inner_code);
                put("msg", msg);
            }}));
        }).produces(MIME.MIME_APPLICATION_JSON);
    }

    public static ResultDef<Exception> errorException(int code, String desc) {
        return new ResultDef<Exception>(code, desc, (ctx, e) -> {
            S._debug(Ctx.logger, log -> {
                log.debug("errorTrace:", e);
            });
            ((HttpCtx.Lazy) ctx::bind).resp().sendError(code, e.getMessage());
        }).produces(MIME.MIME_TEXT_PLAIN);
    }

    /**
     * send text as formatted String (text/plain)
     *
     * @param code   code
     * @param format string format used for String.format
     * @return a lambda takes String[]
     */
    public static ResultDef<Object[]> printf(int code, String format) {
        return new ResultDef<Object[]>(code, format, (ctx, arr) -> {
            ((HttpCtx.Lazy) ctx::bind).resp().send(code, String.format(format, arr));
        }).produces(MIME.MIME_TEXT_PLAIN);
    }

    /**
     * send error as formatted String (text/plain)
     *
     * @param code   code
     * @param format string format used for String.format
     * @return a lambda takes String[]
     */
    public static ResultDef<Object[]> errorf(int code, String format) {
        return new ResultDef<Object[]>(code, format, (ctx, arr) -> {
            ((HttpCtx.Lazy) ctx::bind).resp().sendError(code, String.format(format, arr));
        }).produces(MIME.MIME_TEXT_PLAIN);
    }

    @Override
    public void apply(HttpCtx ctx, T t) {
        handler.apply(ctx, t);
    }

}
