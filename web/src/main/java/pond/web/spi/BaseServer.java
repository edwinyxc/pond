package pond.web.spi;

import org.slf4j.Logger;
import pond.common.f.Callback;
import pond.web.Pond;
import pond.web.Request;
import pond.web.Response;

import java.util.concurrent.Future;

public interface BaseServer {

  Logger logger = org.slf4j.LoggerFactory.getLogger(BaseServer.class);

  //allowed env vars:
  /**
   * Use SSL, [boolean]  when this option triggered,
   * the port is locked to 443
   */
  String SSL = "ssl";

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

  //register process handler
  void handler(Callback.C2<Request, Response> handler);

  void pond(Pond pond);

  Pond pond();

}
