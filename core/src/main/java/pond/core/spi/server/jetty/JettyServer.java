package pond.core.spi.server.jetty;

import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import pond.common.S;
import pond.core.Config;
import pond.core.Pond;
import pond.core.Request;
import pond.core.Response;
import pond.core.exception.UnexpectedException;
import pond.core.misc.HSRequestWrapper;
import pond.core.misc.HSResponseWrapper;
import pond.core.spi.BaseServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static pond.common.S._notNullElse;
import static pond.core.Pond.debug;
import static pond.common.f.Function.F2;
/**
 * @author ed
 */
public class JettyServer implements BaseServer {

    private org.eclipse.jetty.server.Server server;

    private F2<Boolean, Request, Response> dynamicServer
            = (req, resp) -> { S.echo("EMPTY SERVER");return false;};

    //dynamic server
    Handler dyn =
            new AbstractHandler() {
                @Override
                public void handle(String target,
                                   org.eclipse.jetty.server.Request baseRequest,
                                   HttpServletRequest request,
                                   HttpServletResponse response)
                        throws IOException, ServletException {
                    try {
                        baseRequest.setHandled(
                                dynamicServer.apply(
                                        new HSRequestWrapper(request),
                                        new HSResponseWrapper(response)));
                    } catch (Throwable e) {
                        //TODO unwrap (RuntimeException)e;
                        // Jetty throw EofE when client close the connection
                        if (e instanceof EofException) {
                            logger.info("Jetty throw an EOF exception");
                            return;
                        }
                        S._lazyThrow(e);
                    }
                }
            };
    Handler st = new DefaultHandler();


    @Override
    public void listen(int port) {
        server = new org.eclipse.jetty.server.Server(port);

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[]{
                dyn, st
        });
        //bind to server
        server.setHandler(handlerList);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
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
    public void installHandler(F2<Boolean, pond.core.Request, Response> handler) {
        this.dynamicServer = handler;
    }

    @Override
    public void installStatic(StaticFileServer server) {
        if (!(server instanceof JettyStaticFileServer)) {
            throw new RuntimeException("Static Server must be instance of JettyStaticFileServer when you" +
                    "are using Jetty as server");
        }
        this.st = (JettyStaticFileServer) server;
    }

    @Override
    public StaticFileServer staticFileServer(String str) {
        Config config = pond.config;
        return new JettyStaticFileServer(str)
                .allowList(_notNullElse(config.getBool(Config.ALLOW_LIST_DIRECTORY), false))
                .allowMemFileMapping(_notNullElse(config.getBool(Config.ALLOW_MEMORY_FILE_MAPPING), false));
    }

    @Override
    public void pond(Pond pond) {
        this.pond = pond;
    }

    @Override
    public Pond pond() {
        return pond;
    }

    Pond pond;

    public class JettyStaticFileServer extends ServletContextHandler
            implements StaticFileServer {

        ServletHolder holder;

        public JettyStaticFileServer(String dir) {
            String webRoot = pond.pathRelWebRoot(dir);

            File f = new File(webRoot);
            if (!f.exists() || !f.canWrite()) {
                throw new UnexpectedException() {
                    @Override
                    public String brief() {
                        return "File[" + webRoot + "] not valid";
                    }
                };
            }
            debug("static server path : " + webRoot);

            //default false
            this.setResourceBase(webRoot);

            DefaultServlet defaultServlet = new DefaultServlet();
            holder = new ServletHolder(defaultServlet);
            this.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
            holder.setInitParameter("useFileMappedBuffer", "false");
            this.addServlet(holder, "/");
        }

        @Override
        public StaticFileServer allowList(boolean b) {
            this.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", String.valueOf(b));
            return this;
        }

        @Override
        public StaticFileServer welcomeFiles(String... files) {
            this.setWelcomeFiles(files);
            return this;
        }

        @Override
        public StaticFileServer allowMemFileMapping(boolean b) {
            holder.setInitParameter("useFileMappedBuffer", String.valueOf(b));
            return this;
        }
    }
}
