package com.shuimin.jtiny.core.server.jetty;

import com.shuimin.base.S;
import com.shuimin.jtiny.core.server.AbstractServer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ed
 */
public class JettyServer extends AbstractServer {

    private org.eclipse.jetty.server.Server server;


    @Override
    public void listen(int port) {
        server = new org.eclipse.jetty.server.Server(port);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response)
                throws IOException, ServletException {

                chainedHandler.handle(
                    new HSRequestWrapper(request),
                    new HSResponseWrapper(response));
            }
        });
        try {
            server.start();
        } catch (Exception e) {
            S._lazyThrow(e);
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            S._lazyThrow(e);
        }
    }

}
