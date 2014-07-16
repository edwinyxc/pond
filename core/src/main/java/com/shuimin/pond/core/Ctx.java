package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.http.AbstractRequest;
import com.shuimin.pond.core.spi.Logger;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

import static com.shuimin.common.S._for;

/**
 * 访问上下文,用于保持异步工作时的状态
 */
public class Ctx extends TreeMap<String, Object> {
    static Logger logger = Logger.createLogger(Ctx.class);
    Request req;
    Response resp;
    Stack<Mid> mids = new Stack<>();

    public Ctx(Request req, Response resp,
               List<Mid> mids) {
        this.req = new ReqWrapper(req);
        this.put("req", this.req);
        this.resp = new RespWrapper(resp);
        this.put("resp", this.resp);
        List<Mid> _mids = new ArrayList<>(mids);
        Collections.reverse(_mids);
        for (Mid m : _mids) {
            this.mids.push(m);
        }
        try {
            this.putAll(this.req.ctx());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        logger.debug("Main ctx route:" + String.join("->",
                _for(mids).map(Object::toString).join()));
    }


    public Request req() {
        return req;
    }

    public Response resp() {
        return resp;
    }

    public void addMid(List<Mid> midList) {
        Collections.reverse(midList);
        for (Mid m : midList)
            mids.push(m);
    }

    public Callback.C3<Request, Response, Callback.C0> nextMid() {
        if (mids.empty()) return null;
        return mids.pop();
    }

    class RespWrapper implements Response {
        Response res;

        RespWrapper(Response res) {
            this.res = res;
        }

        @Override
        public Response header(String k, String v) {
            return res.header(k, v);
        }

        @Override
        public void sendError(int code, String msg) {
            res.sendError(code, msg);
        }

        @Override
        public void send(int code, String msg) {
            res.send(code, msg);
        }

        @Override
        public Response status(int sc) {
            return res.status(sc);
        }

        @Override
        public OutputStream out() {
            return res.out();
        }

        @Override
        public PrintWriter writer() {
            return res.writer();
        }

        @Override
        public Response write(String s) {
            return res.write(s);
        }

        @Override
        public Response cookie(Cookie c) {
            return res.cookie(c);
        }

        @Override
        public void redirect(String url) {
            res.redirect(url);
        }

        @Override
        public Response contentType(String type) {
            return res.contentType(type);
        }

        @Override
        public Ctx ctx() {
            return Ctx.this;
        }
    }

    class ReqWrapper extends AbstractRequest {
        ReqWrapper(Request req) {
            this.req = req;
        }

        Request req;

        @Override
        public InputStream in() throws IOException {
            return req.in();
        }

        @Override
        public String uri() {
            return req.uri();
        }

        @Override
        public Locale locale() {
            return req.locale();
        }

        @Override
        public Map<String, String[]> headers() {
            return req.headers();
        }

        @Override
        public Map<String, String[]> params() {
            return req.params();
        }

        @Override
        public String method() {
            return req.method();
        }

        @Override
        public String remoteIp() {
            return req.remoteIp();
        }

        @Override
        public Iterable<Cookie> cookies() {
            return req.cookies();
        }

        @Override
        public String characterEncoding() {
            return req.characterEncoding();
        }

        @Override
        public Ctx ctx() {
            return Ctx.this;
        }
    }
}
