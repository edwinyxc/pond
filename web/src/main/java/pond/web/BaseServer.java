package pond.web;

import org.slf4j.Logger;
import pond.common.S;
import pond.common.f.Callback;

import java.util.concurrent.Future;

/**
 * SPI handle basic http server
 * This server SHOULD
 * 1) Implements Request & Response
 * 2) Implements the HTTP protocol
 */
public interface BaseServer {

  Logger logger = org.slf4j.LoggerFactory.getLogger(BaseServer.class);

  /**
   * CONFIGS
   */
  String PORT         = "port";
  String HTTP_PARSER_HEADER_CASE_SENSITIVE = "http_parser_header_case_sensitive";
  String SO_BACKLOG   = "so_backlog";
  String SO_KEEPALIVE = "so_keepalive";
  String SSL = "ssl";

  static boolean IS_HEADER_SENSITIVE() {
    return "true".equals(S.config.get(BaseServer.class, BaseServer.HTTP_PARSER_HEADER_CASE_SENSITIVE));
  }


  /**
   * Bind configured PORT and start server, normally this operation will block the thread.
   * PORT is configured through System.setProperty("pond.web.BaseServer.port",XXX) with a default value 9090
   */
  Future listen();

  /**
   * stop server
   */
  Future stop(Callback<Future> listener) throws Exception;

  /**
   * Register the processHandler
   */
  void registerHandler(CtxHandler handler);

  /**
   * add pond
   */
  void pond(Pond pond);

  Pond pond();

}
