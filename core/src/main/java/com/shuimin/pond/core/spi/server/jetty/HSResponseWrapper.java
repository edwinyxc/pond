package com.shuimin.pond.core.spi.server.jetty;

import com.shuimin.common.S;
import com.shuimin.pond.core.Response;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author ed
 */
public class HSResponseWrapper implements Response {

    HttpServletResponse _resp;
    private boolean hasSend = false;

    public HSResponseWrapper(HttpServletResponse hsr) {
        _resp = hsr;
    }

    @Override
    public Response header(String k, String v) {
        _resp.addHeader(k, v);
        return this;
    }

    @Override
    public void sendError(int code, String msg) {
        try {
            _resp.sendError(code,msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(int code, String msg) {
        if (hasSend) return;
        _resp.setStatus(code);
        try {
            _resp.getWriter().print(msg);
        } catch (IOException e) {
            S._lazyThrow(e);
        }finally {
            try {
                _resp.flushBuffer();
            } catch (IOException e) {
                S._lazyThrow(e);
            }
        }
        hasSend = true;
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
