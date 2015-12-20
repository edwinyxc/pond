package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.f.Callback;

import java.util.LinkedList;

public class Executor {

  final static Logger logger = LoggerFactory.getLogger(Executor.class);

  LinkedList<Callback.C2<? extends ExecutionContext, Service>> interceptors =
      new LinkedList<>();

  public ExecutionContext createCtx(String user) {
    return new ExecutionContext(user);
  }

  public ExecutionContext exec(ExecutionContext ctx, Service... services) {

    for (Service serv : services) {
      //interceptor
      for(Callback.C2 interceptor : interceptors){
        interceptor.apply(ctx, serv);
      }
      if(ctx.stop){
        logger.debug("CTX@" + ctx + "stopped AT SERV@" + serv);
        break;
      }
      //apply service
      serv.init(ctx);
      serv.call();
    }

    return ctx;
  }

  public Executor interceptor(Callback.C2<? extends ExecutionContext, Service> cb) {
    interceptors.add(cb);
    return this;
  }

}
