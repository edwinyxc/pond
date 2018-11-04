package pond.net;

import org.slf4j.Logger;
import pond.common.f.Callback;

import java.util.concurrent.Future;

public interface Server {

    Logger logger = org.slf4j.LoggerFactory.getLogger(Server.class);

    /**
     * CONFIGS
     */
    String PORT         = "port";
    String HTTP_PARSER_HEADER_CASE_SENSITIVE = "http_parser_header_case_sensitive";
    String SO_BACKLOG   = "so_backlog";
    String SO_KEEPALIVE = "so_keepalive";
    String SSL = "ssl";
    //configurations
    /***** Internal ****/
//  public final static String CONFIG_FILE_NAME = "config";

    /**
     * web root
     */
    String CONFIG_WEB_ROOT = "web_root";

    /**
     * class root
     */
    String CONFIG_CLASS_ROOT = "class_root";

    /*** Basic ***/


    /**
     * Bind configured PORT and start server, normally this operation will block the thread.
     * PORT is configured through System.setProperty("pond.web.BaseServer.port",XXX) with a default value 9090
     */
    Future listen() throws InterruptedException;

    Future listen(int port) throws InterruptedException;

    /**
     * stop server
     */
    Future stop(Callback<Future> listener) throws Exception;

    default void stop() throws Exception{
        stop(null);
    }
}
