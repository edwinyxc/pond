package pond.core;

import pond.common.f.Function;

public class Executor {

  Function.F2<Boolean, ExecutionContext, Service> auth_method =
      (ctx, serv) -> true;

  ExecutionContext createCtx(String user){
    return new ExecutionContext(user);
  }

  public ExecutionContext exec(ExecutionContext ctx, Service... services) {

    for (Service serv : services) {
      serv.init(ctx);
      if(auth_method.apply(ctx, serv)) {
        serv.call();
      } else {
        ctx.err("service authentication failed: " + serv);
      }
    }

    return ctx;
  }

  //TODO shared-ctx based parallel OR piped-stream based IPC??

//  public void exec_in_parallel(ExecutorService executorService){
//    executorService.submit();
//    executorService.
//  }

}
