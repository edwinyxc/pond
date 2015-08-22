package pond.web.spi.server;

import pond.common.f.Callback;
import pond.web.Pond;
import pond.web.Request;
import pond.web.Response;
import pond.web.spi.BaseServer;

public abstract class AbstractServer implements BaseServer {

  private Pond pond;
  private Callback.C2<Request, Response> handler;

  protected Callback.C2<Request, Response> handler() {
    return this.handler;
  }

  @Override
  public void pond(Pond pond) {
    this.pond = pond;
  }

  @Override
  public Pond pond() {
    return pond;
  }

  @Override
  public void handler(Callback.C2<Request, Response> handler) {
    this.handler = handler;
  }


}
