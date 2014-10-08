package pond.core.spi.server.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.Jetty;
import pond.common.S;
import pond.common.f.Callback;
import pond.core.Pond;
import pond.core.Request;
import pond.core.Response;
import pond.core.exception.UnexpectedException;
import pond.core.misc.HSRequestWrapper;
import pond.core.misc.HSResponseWrapper;
import pond.core.spi.BaseServer;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static pond.core.Pond.debug;

/**
 * @author ed
 */
public class JettyServer implements BaseServer {

    private org.eclipse.jetty.server.Server server;

    private Callback.C2<Request, Response> dynamicServer
            = (req, resp) -> S.echo("EMPTY SERVER");

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
                        dynamicServer.apply(
                                new HSRequestWrapper(request),
                                new HSResponseWrapper(response));
                    } catch (Throwable e) {
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
    public void installHandler(Callback.C2<pond.core.Request, Response> handler) {
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
        return new JettyStaticFileServer(str);
    }

    public class JettyStaticFileServer extends ResourceHandler
            implements StaticFileServer {

        public JettyStaticFileServer(String dir) {
            String webRoot = Pond.pathRelWebRoot(dir);

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
            this.setDirectoriesListed(false);
            this.setResourceBase(webRoot);
            //prevent NullPointerException problem
            this.setStylesheet("");
        }

        @Override
        public StaticFileServer allowList(boolean b) {
            this.setDirectoriesListed(b);
            return this;
        }

        @Override
        public StaticFileServer welcomeFiles(String... files) {
            this.setWelcomeFiles(files);
            return this;
        }
    }
}
