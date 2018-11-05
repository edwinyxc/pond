package pond.net;

public interface Server {

 /**
* Bind configured PORT and start server, normally this operation will block the thread.
* PORT is configured through System.setProperty("pond.web.BaseServer.port",XXX) with a default value 9090
*/
  void listen();
}
