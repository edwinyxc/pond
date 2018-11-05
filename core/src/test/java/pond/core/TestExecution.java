package pond.core;

import org.junit.Ignore;
import org.junit.Test;
import pond.common.S;

import java.util.ArrayList;
import java.util.concurrent.*;

public class TestExecution {

    interface CtxLogger extends Ctx {
        default Executable tomLog(){
            return Executable.of("tom", ctx->{

                S.echo("Tom: Thread:"+this.currentThread()+" Ctx:", ctx.jobs());
            });
        }

        default void log(){
            Executable next = this.peek();
            S.echo("Peeking: " + next.name());
        }

        static Executable log = Executable.of("logger", ctx -> {
            ((CtxLogger) ctx).log();
        });
    }

    interface CtxRunner extends Ctx{

        default void addInterceptorForEach(Executable interceptor){
            var origin = this.jobs();
            var newList = new ArrayList<Executable>();
            for(Executable o : origin){
                newList.add(interceptor);
                newList.add(o);
            }
            this.jobs().clear();
            this.jobs().addAll(newList);
        }

        static Executor singlePool = Executors.newFixedThreadPool(1);

        default CompletableFuture<Void> runAsync(){
            Executable next;
            CompletableFuture<Void> cf = null;
            while (null != (next = this.next())){
                Executable finalNext = next;
                if(cf == null){
                    cf = CompletableFuture.runAsync(() -> {
                        finalNext.body().apply(this);
                    }, singlePool);
                }else {
                    cf = cf.thenRunAsync(() -> {
                        finalNext.body().apply(this);
                    });
                }
            }
            return cf;
        }
    }

    @Test
    public void java11_traits(){
        CtxBase base = new CtxBase();
        var ctx = (Ctx & CtxRunner & CtxLogger) () -> base;
        ctx.push(
            Executable.of("1", _c -> _c.properties().put("user1", "new_user")),
            Executable.of("2", _c -> _c.properties().put("user2", "new_user")),
            Executable.of("3", _c -> _c.properties().put("user3", "new_user")),
            Executable.of("4", _c -> _c.properties().put("user4", "new_user"))
        );
        ctx.addInterceptorForEach(ctx.tomLog());
        try {
            ctx.runAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }



  /*
  Service simple_add = new Service(ctx -> {
    int a = (int) ctx.get("$1");
    int b = (int) ctx.get("$2");
    ctx.push(a + b);
  });

  Service fold_add_ten = new Service(ctx -> {
    int last = (int) ctx.pop();
    ctx.push(last + 10);
  });
  */


    @Test @Ignore
    public void register() {


    }

    @Test @Ignore
    public void localCurrentCtx() {
    /*
    Context ctx = new Context("tmp");
    new Thread(() -> {
      ctx.push(10);
      ctx.exec(fold_add_ten, fold_add_ten, new Service(c -> {
        Assert.assertEquals(ctx, Context.current());
      }));
    }).run();
    */
    }

    @Test
    public void basic() {
/*
    Context ctx = new Context("user");
    //dynamic execution procedure
    S.echo("time usage:" + S.time(() -> {
      //test fold
      ctx.push(10);
      S.echo(ctx.exec(
          fold_add_ten,
          fold_add_ten,
          fold_add_ten,
          fold_add_ten,
          new Service(c -> {
            c.set("$1", 12);
            c.set("$2", 14);
          }),
          simple_add,
          new Service(c -> {
            int add_result = (int) c.pop();
            S.echo(add_result);
            int other_result = (int) c.pop();
            S.echo(other_result);
            c.push(add_result + other_result);
          })
      ));
    }));
*/
    }
}
