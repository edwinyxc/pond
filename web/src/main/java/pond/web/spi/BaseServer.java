package pond.web.spi;

import org.slf4j.Logger;
import pond.common.f.Callback;
import pond.web.Pond;
import pond.web.Request;
import pond.web.Response;

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
   * PORT
   */
  String PORT = "port";

  /**
   * use the registered env("port") to get the listen port
   * this method will block the thread
   */
  Future listen();

  /**
   * stop server
   *
   * @throws Exception
   */
  Future stop(Callback<Future> listener) throws Exception;

  /**
   * Register the processHandler
   * @param handler
   */
  void registerHandler(Callback.C2<Request, Response> handler);

  void pond(Pond pond);

  Pond pond();

}
