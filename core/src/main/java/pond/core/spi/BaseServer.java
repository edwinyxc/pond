package pond.core.spi;

import org.slf4j.Logger;
import pond.common.f.Callback;
import pond.core.PondAware;
import pond.core.Request;
import pond.core.Response;

public interface BaseServer extends PondAware{

    Logger logger = org.slf4j.LoggerFactory.getLogger(BaseServer.class);

    //allowed env vars:
    /**
     Use SSL, [boolean]  when this option triggered,
     the port is locked to 443
     */
    String SSL = "ssl";

    /**
     * PORT
     */
    String PORT = "port";

    /*
     * max in-queue connection
     */
    String BACK_LOG = "backlog";

    /**
     * locale
     */
    String LOCALE = "locale";

    /**
     use the registered env("port") to get the listen port
     */
    void listen() throws Exception;

    //register process handler
    void handler(Callback.C2<Request, Response> handler);

    //set Config
    BaseServer env(String key, Object whatever);

    //get env
    Object env(String key );
}
