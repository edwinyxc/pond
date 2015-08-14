package pond.core;

import org.junit.After;
import pond.core.spi.BaseServer;

public class TestRender {

  Pond app;

  static {
    System.setProperty(BaseServer.PORT, "9090");
  }

  public void before() {

  }

  @After
  public void close(){
    if(app != null)
      app.stop();
  }

}
