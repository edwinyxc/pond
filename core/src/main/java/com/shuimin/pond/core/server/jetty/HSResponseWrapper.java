package com.shuimin.pond.core.server.jetty;

import com.shuimin.common.S;
import com.shuimin.pond.core.http.Response;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static com.shuimin.common.S._throw;

/**
 * @author ed
 */
public class HSResponseWrapper implements Response {

    HttpServletResponse _resp;

    public HSResponseWrapper(HttpServletResponse hsr) {
        _resp = hsr;
    }

    @Override
    public Response header(String k, String v) {
        _resp.addHeader(k, v);
        return this;
    }

    @Override
    public void send(int code) {
        _resp.setStatus(code);
        try {
            _resp.flushBuffer();
        } catch (IOException e) {
            _throw(e);
        }
    }

    @Override
    public void sendError(int code, String msg) {
        try {
            _resp.sendError(code,msg);
        } catch (IOException e) {
            _throw(e);
        }
    }

    @Override
    public void sendFile(File file) {
        _resp.setStatus(200);
        try(FileInputStream in = new FileInputStream(file)){
            S.stream.write(in,_resp.getOutputStream());
        } catch (IOException e) {
           _throw(e);
        }
    }

    @Override
    public Response status(int sc) {
        _resp.setStatus(sc);
        return this;
    }

    //    @Override
//    public PrintWriter writer() {
//        try {
//            return _resp.getWriter();
//        } catch (IOException ex) {
//            S._lazyThrow(ex);
//            return null;
//        }
//    }
    @Override
    public Response cookie(Cookie c) {
        _resp.addCookie(c);
        return this;
    }

    @Override
    public void redirect(String url) {
        try {
            _resp.sendRedirect(url);
        } catch (IOException ex) {
            S._lazyThrow(ex);
        }
    }

    @Override
    public Response contentType(String type) {
        _resp.setContentType(type);
        return this;
    }

    @Override
    public HttpServletResponse raw() {
        return _resp;
    }

    @Override
    public OutputStream out() {
        try {
            return _resp.getOutputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
            S._lazyThrow(ex);
        }
        return null;
    }

    @Override
    public PrintWriter writer() {
        try {
            return _resp.getWriter();
        } catch (IOException ex) {
            ex.printStackTrace();
            S._lazyThrow(ex);
        }
        return null;
    }

    @Override
    public Response write(String s) {
        this.writer().print(s);
        return this;
    }

}
