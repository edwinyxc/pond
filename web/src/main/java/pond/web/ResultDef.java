package pond.web;

import pond.common.JSON;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Tuple;
import pond.web.http.MimeTypes;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;


/**
 * Created by ed on 3/8/17.
 */
public class ResultDef<T> implements Callback.C2<Ctx, T> {
    final Callback.C2<Ctx, T> cb;
    final Integer httpStatusCode;
    final String description;

    private ResultDef(Integer code, String desc, Callback.C2<Ctx, T> cb) {
        this.httpStatusCode = code;
        this.cb = cb;
        this.description = desc;
    }

    public static <T> ResultDef<T> any(Integer code, String desc, Callback.C2<Ctx, T> cb) {
        return new ResultDef<T>(code, desc, cb);
    }


    /**
     * send the ctx dump
     *
     * @return a lambda ignores any input
     */
    public static ResultDef<Void> debug() {
        return new ResultDef<>(200, "debug", (ctx, t) -> {
            ((HttpCtx) ctx).resp.send(200, ctx.toString());
        });
    }

    /**
     * send as text/plain, it the input is null, send "ok"
     *
     * @param desc description of this result
     * @return a lambda takes a string value
     * @see Render#text(String)
     */
    public static ResultDef<String> text(String desc) {
        return new ResultDef<>(200, desc, (ctx, t) -> {
            ((HttpCtx) ctx).resp.render(Render.text(S.avoidNull(t, "ok")));
        });
    }


    /**
     * @return
     */
    public static ResultDef<Void> ok() {
        return new ResultDef<>(200, "ok", (ctx, t) -> {
            ((HttpCtx) ctx).resp.render(Render.text("OK"));
        });
    }

    /**
     * send desc as is (text/plain)
     *
     * @param desc description
     * @param code httpStatusCode
     * @return a lambda ignores its input and send the desc
     */
    public static ResultDef<Void> lazy(int code, String desc) {
        return new ResultDef<>(code, desc, (ctx, t) -> {
            ((HttpCtx) ctx).resp.send(code, S.avoidNull(desc, "ok"));
        });
    }

    /**
     * send as application/json, it the input is null, send "{}"
     *
     * @param desc description of this result
     * @return a lambda takes a Object value
     * @see Render#json(Object)
     */
    public static <T> ResultDef<T> json(int code, String desc) {
        return new ResultDef<>(code, desc, (ctx, t) -> {
            Response resp = ((HttpCtx) ctx).resp;
            resp.contentType(MimeTypes.MIME_APPLICATION_JSON);
            resp.send(code, JSON.stringify(t));
        });
    }

    /**
     * send as application/json, it the input is null, send "{}"
     *
     * @param desc description of this result
     * @return a lambda takes a Object value
     * @see Render#json(Object)
     */
    public static <T> ResultDef<T> json(String desc) {
        return new ResultDef<>(200, desc, (ctx, t) -> {
            ((HttpCtx) ctx).resp.render(Render.json(t));
        });
    }

    /**
     * send as application/json
     *
     * @param desc description of this result
     * @return a lambda takes a Tuple (Object, Integer)
     * @see Render#page(java.lang.Object, int)
     */
    public static ResultDef<Tuple<Object, Integer>> page(String desc) {
        return new ResultDef<>(200, desc, (ctx, t) -> {
            ((HttpCtx) ctx).resp.render(Render.page(t._a, t._b));
        });
    }

    /**
     * send file based at the file's ext
     *
     * @param desc description of this result
     * @return a lambda takes a File input
     * @see Render#file
     */
    public static ResultDef<File> file(String desc) {
        return new ResultDef<>(200, desc, (ctx, t) -> {
            ((HttpCtx) ctx).resp.render(Render.file(t));
        });
    }

    /**
     * send download file as octet-stream with filename defined
     *
     * @param desc description of this result
     * @return a lambda takes a Tuple(String, OutputStream)
     */
    public static ResultDef<Tuple<String, Callback<OutputStream>>> download(String desc) {
        return new ResultDef<>(200, desc, (ctx, t) -> {
            String filename = t._a;
            Callback<OutputStream> outputStreamCallback = t._b;
            Response resp = ((HttpCtx) ctx).resp;
            resp.header("Content-Disposition", String.format("attachment;filename=%s", filename));
            resp.contentType("application/octet-stream");
            outputStreamCallback.apply(resp.out());
            resp.send(200);
        });
    }

    //errors

    public static ResultDef<String> error(int code, String desc) {
        return new ResultDef<>(code, desc, (ctx, msg) -> {
            ((HttpCtx) ctx).resp.sendError(code, msg);
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
        return new ResultDef<>(code, desc, (ctx, t) -> {
            int inner_code = t._a;
            String msg = t._b;
            ((HttpCtx) ctx).resp.sendError(code, JSON.stringify(new HashMap<String, Object>() {{
                put("code", inner_code);
                put("msg", msg);
            }}));
        });
    }

    public static ResultDef<Exception> errorException(int code, String desc) {
        return new ResultDef<>(code, desc, (ctx, e) -> {
            S._debug(Pond.logger, log -> {
                log.debug("errorTrace:", e);
            });
            ((HttpCtx) ctx).resp.sendError(code, e.getMessage());
        });
    }

    /**
     * send text as formatted String (text/plain)
     *
     * @param code   code
     * @param format string format used for String.format
     * @return a lambda takes Tuple(String[])
     */
    public static ResultDef<Object[]> printf(int code, String format) {
        return new ResultDef<>(code, format, (ctx, arr) -> {
            ((HttpCtx) ctx).resp.send(code, String.format(format, arr));
        });
    }

    /**
     * send error as formatted String (text/plain)
     *
     * @param code   code
     * @param format string format used for String.format
     * @return a lambda takes Tuple(String[])
     */
    public static ResultDef<Object[]> errorf(int code, String format) {
        return new ResultDef<>(code, format, (ctx, arr) -> {
            ((HttpCtx) ctx).resp.sendError(code, String.format(format, arr));
        });
    }

    @Override
    public void apply(Ctx ctx, T t) {
        cb.apply(ctx, t);
    }

}
