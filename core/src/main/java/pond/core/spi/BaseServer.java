package pond.core.spi;

import org.slf4j.Logger;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.PondAware;
import pond.core.Request;
import pond.core.Response;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public interface BaseServer extends PondAware{

    Logger logger = org.slf4j.LoggerFactory.getLogger(BaseServer.class);

    //allowed env vars:
    /**
     Use SSL, [boolean]  when this option triggered,
     the port is locked to 443
     */
    String SSL = "BaseServer.ssl";

    /**
     * PORT
     */
    String PORT = "BaseServer.port";

    /*
     * max in-queue connection
     */
    String BACK_LOG = "BaseServer.backlog";

    /**
     * locale
     */
    String LOCALE = "BaseServer.locale";

    /**
     use the registered env("port") to get the listen port
     */
    void listen() throws Exception;

    //register process handler
    void handler(Callback.C2<Request, Response> handler);

    void executor(ExecutorService executor);

    //get env
    Object env(String key );

    void regEnv(Function<Object, String> f);
}
