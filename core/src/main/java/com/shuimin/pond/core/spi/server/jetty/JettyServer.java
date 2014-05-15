package com.shuimin.pond.core.spi.server.jetty;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.Response;
import com.shuimin.pond.core.spi.BaseServer;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ed
 */
public class JettyServer implements BaseServer {

    private org.eclipse.jetty.server.Server server;

    private Callback._2<com.shuimin.pond.core.Request,Response> handler
        = (req,resp) -> S.echo("EMPTY SERVER") ;

    @Override
    public void listen(int port) {
        server = new org.eclipse.jetty.server.Server(port);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response)
                throws IOException, ServletException {

                handler.apply(
                    new HSRequestWrapper(request),
                    new HSResponseWrapper(response));
            }
        });
        try {
            server.start();
        } catch (Exception e) {
            // Jetty throw EofE when client close the connection
            if( e instanceof EofException){
                S.echo("Jetty throw an EOF exception");
                return;
            }
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

    @Override
    public void installHandler(Callback._2<com.shuimin.pond.core.Request, Response> handler) {
        this.handler = handler;
    }

}