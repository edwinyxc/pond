package pond.core.spi;

import pond.core.Request;
import pond.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pond.common.f.Callback.C2;
/**
 * Created by ed on 2014/5/8.
 */
public interface BaseServer {
    static Logger logger = LoggerFactory.getLogger(BaseServer.class);

    public void listen(int port);

    public void stop();

    public void installHandler(C2<Request, Response> handler);

    public void installStatic(StaticFileServer server);

    public StaticFileServer staticFileServer(String str);

    public interface StaticFileServer{

        public StaticFileServer allowList(boolean b);

        public StaticFileServer welcomeFiles(String... files);

    }
}
