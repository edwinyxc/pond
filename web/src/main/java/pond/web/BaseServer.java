package pond.web;

import org.slf4j.Logger;
import pond.common.f.Callback;

import java.util.concurrent.Future;

/**
 * SPI of basic http server
 * This server SHOULD
 * 1) Implements Request & Response
 * 2) Meets the HTTP protocol
 * 3)
 */
public interface BaseServer {

  Logger logger = org.slf4j.LoggerFactory.getLogger(BaseServer.class);

  /**
   * CONFIGS
   */
  final static String PORT         = "port";
  final static String SO_BACKLOG   = "so_backlog";
  final static String SO_KEEPALIVE = "so_keepalive";

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
