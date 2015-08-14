package pond.web.spi;

import org.slf4j.Logger;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.web.Pond;
import pond.web.Request;
import pond.web.Response;
import pond.web.spi.server.HttpContentParser;

import java.util.concurrent.Future;

public interface BaseServer {

  Logger logger = org.slf4j.LoggerFactory.getLogger(BaseServer.class);

  //allowed env vars:
  /**
   * Use SSL, [boolean]  when this option triggered,
   * the port is locked to 443
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
   * use the registered env("port") to get the listen port
   * this method will block the thread
   */
  void listen();

  /**
   * stop server
   *
   * @throws Exception
   */
  void stop(Callback<Future> listener) throws Exception;


  //register process handler
  void handler(Callback.C2<Request, Response> handler);


  //get env
  Object env(String key);

  void regEnv(Function<Object, String> f);

  void regContentParser(HttpContentParser parser);

  void pond(Pond pond);

  Pond pond();

}
