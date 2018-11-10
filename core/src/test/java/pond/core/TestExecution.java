package pond.core;

import org.junit.Ignore;
import org.junit.Test;
import pond.common.S;
import pond.common.f.Tuple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;

public class TestExecution {

    interface CtxLogger extends Ctx {
        default Executable tomLog(){
            return Executable.of("tom", ctx->{

                S.echo("Tom: Thread:"+this.currentThread()+" Next:", this.peek());
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

        default CompletableFuture<Void> runASync_(){
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



     class CountAndLogSubscriber extends SubmissionPublisher<Ctx> implements  Flow.Subscriber<Ctx> {

        Flow.Subscription subscription;

        List<Executable> executables = new LinkedList<>();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public void onNext(Ctx item) {
            S.echo("End Got Ctx" + item.delegate().hashCode());
            if(item.peek() != null){
                var e = item.next();
                e.body().apply(item);
                this.executables.add(e);
            }
            subscription.cancel();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() { S.echo("CountAndLogSubscriber done");}
    };



    @Test
    public void runAsync() throws ExecutionException, InterruptedException {

        CtxFlowProcessor end = new CtxFlowProcessor();
        CtxBase base = new CtxBase();
        var ctx = (Ctx & CtxRunner & CtxLogger) () -> base;

        end.subscribe(ctx.flowProcessor());

        ctx.push(
            Executable.of("1", _c -> {
                _c.properties().put("user1", "new_user");
                S.echo("Hello!!");
            }),
            Executable.of("2", _c -> _c.properties().put("user2", "new_user")),
            Executable.of("3", _c -> _c.properties().put("user3", "new_user")),
            Executable.of("4", _c -> _c.properties().put("user4", "new_user"))
        );
        //tom log on end subscriber
        ctx.addInterceptorForEach(ctx.tomLog().flowTo(end));
//        ctx.runReactiveFlow(_ctx -> new ArrayList<>(){{
//            add(Tuple.pair(end, List.of(1,3,5,7)));
//            add(Tuple.pair(ctx.flowProcessor(), List.of(0,2,4,6)));
//        }});

        ctx.runReactiveFlow(Ctx.ReactiveFlowConfig.DEFAULT);
        await().atMost(10000, TimeUnit.MILLISECONDS)
            .until(() -> {
                S.echo("size ", end.handled.size());
                return end.handled.size() == 4;
        });
    }

    @Test
    public void subAndPub(){
        CtxBase base = new CtxBase();
        var ctx = (Ctx & CtxRunner & CtxLogger) () -> base;
        //sub push when got
        var subAndPut_subscriber = new ExecutableSubscriber(ctx, Ctx::push);
        //pub
        var publisher = new SubmissionPublisher<Executable>();
        publisher.subscribe(subAndPut_subscriber);

        Executable[] execs = {
            Executable.of("1", _c -> {
                _c.properties().put("user1", "new_user");
                S.echo("Hello!!");
            }),
            Executable.of("2", _c -> _c.properties().put("user2", "new_user")),
            Executable.of("3", _c -> _c.properties().put("user3", "new_user")),
            Executable.of("4", _c -> _c.properties().put("user4", "new_user"))
        };
        try {
            SubmissionPublisher<Executable> finalPublisher = publisher;
            CompletableFuture.runAsync(() -> {
                S._for(execs).each(exec ->{
                    try {
                        Thread.sleep(new Random().nextInt(1000));
                        finalPublisher.submit(exec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                finalPublisher.close();
            })
                .thenRun(()-> {
                    await().atMost(10000, TimeUnit.MILLISECONDS)
                        .until(() -> ctx.jobs().size() == 4);
                    ctx.addInterceptorForEach(ctx.tomLog());
                }).get();
            ctx.runASync_().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        var runAtOnce_sub = new ExecutableSubscriber(ctx, (c, exec) -> {
            c.push(exec);
            var next = c.next();
            S.echo("c.jobs.size", c.jobs().size(), "next", next);
            if(next != null){
                S.echo("run at once", next);
                next.body().apply(c);
            }

        });

        publisher = new SubmissionPublisher<>();

        publisher.subscribe(runAtOnce_sub);
         try {
             SubmissionPublisher<Executable> finalPublisher1 = publisher;
             CompletableFuture.runAsync(() -> {
                S._for(execs).each(exec ->{
                    try {
                        Thread.sleep(new Random().nextInt(1000));
                        finalPublisher1.submit(exec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                finalPublisher1.close();
            }).get();
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
