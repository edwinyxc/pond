package pond.core;

import pond.common.f.Callback;
import pond.core.http.AbstractRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

import static pond.common.S._for;

/**
 * Execution Context, attached to a single thread.
 */
public class Ctx extends TreeMap<String, Object> {
    static Logger logger = LoggerFactory.getLogger(Ctx.class);
    Request req;
    Response resp;
    // When this is true, Ctx stops
    // triggering the following handlers outside the
    // server (A symbol represents the Response has been sent).
    boolean isHandled = false;
    //current pond
    public Pond pond;
    Stack<Mid> mids = new Stack<>();

    public Ctx(Request req,
               Response resp,
               Pond pond,
               List<Mid> mids) 
    {
        this.req = new ReqWrapper(req);
        this.put("req", this.req);
        this.resp = new RespWrapper(resp);
        this.put("resp", this.resp);
        this.pond = pond;
        this.put("pond", this.pond);

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

    public boolean isHandled() {
        return isHandled;
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
            Ctx.this.isHandled = true;
        }

        @Override
        public void send(int code, String msg) {
            res.send(code, msg);
            Ctx.this.isHandled = true;
        }

        @Override
        public Response status(int sc) {
            Ctx.this.isHandled = true;
            return res.status(sc);
        }

        @Override
        public OutputStream out() {
            //TODO get out == handled?
            Ctx.this.isHandled = true;
            return res.out();
        }

        @Override
        public PrintWriter writer() {
            Ctx.this.isHandled = true;
            return res.writer();
        }

        @Override
        public Response write(String s) {
            Ctx.this.isHandled = true;
            return res.write(s);
        }

        @Override
        public Response cookie(Cookie c) {
            return res.cookie(c);
        }

        @Override
        public void redirect(String url) {
            Ctx.this.isHandled = true;
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