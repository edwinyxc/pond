package pond.core;

import org.junit.Ignore;
import org.junit.Test;
import pond.common.S;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;

public class TestExecution {

    interface CtxLogger extends Ctx {
        default CtxHandler<Ctx> tomLog(){
            return  ctx->{

                S.echo("Tom: Thread:"+this.currentThread()+" Next:", this.current());
            };
        }

    }

    interface CtxRunner extends Ctx{

        default void addInterceptorForEach(CtxHandler interceptor){
            var origin = this.jobs();
            var newList = new ArrayList<CtxHandler>();
            for(CtxHandler o : origin){
                newList.add(interceptor);
                newList.add(o);
            }
            this.jobs().clear();
            this.jobs().addAll(newList);
        }


    }

     class CountAndLogSubscriber extends SubmissionPublisher<Ctx> implements  Flow.Subscriber<Ctx> {

        Flow.Subscription subscription;

        List<CtxHandler> ctxHandlers = new LinkedList<>();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public void onNext(Ctx item) {
            S.echo("End Got Ctx" + item.delegate().hashCode());
            if(item.current() != null){
                var e = item.next();
                e.apply(item);
                this.ctxHandlers.add(e);
            }
            subscription.cancel();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() { S.echo("CountAndLogSubscriber done");}
    }

    @Test
    public void calculationTest() {
        Entry<Integer> ACC = new Entry<Integer>("ACC");
        Entry<Integer> ADDER = new Entry<Integer>("ADDER");
        CtxBase base = new CtxBase();
        var ctx = (Ctx)()-> base;

        ctx.pushAll(List.of(
            CtxHandler.provide(ACC, 1),
            CtxHandler.provide(ADDER, 2),
            CtxHandler.process(
                ADDER, ACC,
                (adder, acc) -> adder + acc ,
                ACC
            )
        ));
        ctx.run();
        S.echo(ctx.get(ACC));
    }


    @Test
    public void runReactiveTest() {

        CtxFlowProcessor log = new CtxFlowProcessor("log");
        CtxFlowProcessor hello = new CtxFlowProcessor("hello");
        CtxFlowProcessor another = new CtxFlowProcessor("another");
        CtxBase base = new CtxBase();
        var ctx = (Ctx & CtxRunner & CtxLogger) () -> base;

        //end.subscribe(ctx.flowProcessor());

        ctx.pushAll(List.of(
            CtxHandler.of(_c -> {
                _c.properties().put("user1", "new_user");
                S.echo("Hello!");
            }).flowTo(hello),
             _c -> {
                _c.properties().put("user2", "new_user");
                    S.echo("Hello!!");
             },
             _c -> {
                _c.properties().put("user3", "new_user");
                 S.echo("Hello!!!");
             },
            _c -> {
                _c.properties().put("user4", "new_user");
                S.echo("Hello!!!!");
            },
            CtxHandler.of(c -> {S.echo("Say Hello to another");}).flowTo(another),
            CtxHandler.of(c -> {S.echo("Say Hello to another!!");}).flowTo(another)
        ));
        //tom log on end subscriber
        ctx.addInterceptorForEach(ctx.tomLog().flowTo(log));
        S.echo(ctx.properties() , "job size", ctx.jobs().size());
//        ctx.runReactiveFlow(_ctx -> new ArrayList<>(){{
//            add(Tuple.pair(end, List.of(1,3,5,7)));
//            add(Tuple.pair(ctx.flowProcessor(), List.of(0,2,4,6)));
//        }});

        ctx.runReactiveFlow(Ctx.ReactiveFlowConfig.DEFAULT);
        await().atMost(100000, TimeUnit.MILLISECONDS)
            .until(() -> {
                S.echo("size ", log.handled.size(), another.handled.size(), hello.handled.size());
                S.echo(ctx.properties());
                return log.handled.size() == 6 && another.handled.size() == 2 && hello.handled.size() == 1;
        });
    }

    @Ignore
    @Test
    public void runReactiveTest2() {

        CtxFlowProcessor hello = new CtxFlowProcessor("hello");
        CtxFlowProcessor another = new CtxFlowProcessor("another");
        CtxFlowProcessor heavy = new CtxFlowProcessor("heavy").executor(Executors.newSingleThreadExecutor());
        CtxBase base = new CtxBase();
        var ctx = (Ctx & CtxRunner & CtxLogger) () -> base;

        //end.subscribe(ctx.flowProcessor());

        ctx.pushAll(List.of(
            CtxHandler.of(_c -> {
                _c.properties().put("user1", "new_user");
                S.echo("Hello!");
            }).flowTo(hello),
            _c -> {
                _c.properties().put("user2", "new_user");
                S.echo("Hello!!");
            },
            CtxHandler.of(_c -> {
                _c.properties().put("user3", "new_user");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                S.echo("Hello!!!");
            }).flowTo(heavy),
            _c -> {
                _c.properties().put("user4", "new_user");
                S.echo("Hello!!!!");
            },
            CtxHandler.of(_c -> {
                _c.properties().put("user4", "new_user");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                S.echo("Hello!!!");
            }).flowTo(heavy),
            CtxHandler.of(c -> {S.echo("Say Hello to another");}).flowTo(another),
            CtxHandler.of(c -> {S.echo("Say Hello to another!!");}).flowTo(another)
        ));
        //tom log on end subscriber
        S.echo(ctx.properties() , "job size", ctx.jobs().size());
//        ctx.runReactiveFlow(_ctx -> new ArrayList<>(){{
//            add(Tuple.pair(end, List.of(1,3,5,7)));
//            add(Tuple.pair(ctx.flowProcessor(), List.of(0,2,4,6)));
//        }});

        ctx.runReactiveFlow(Ctx.ReactiveFlowConfig.DEFAULT);

        S.echo("ReactiveStream would not block!");
        await().atMost(100000, TimeUnit.MILLISECONDS)
            .until(() -> {
                S.echo("size ", ctx.flowProcessor().handled.size(),
                    another.handled.size(), hello.handled.size(), heavy.handled.size());
                //S.echo(ctx.properties());
                return ctx.flowProcessor().handled.size() == 2
                           && another.handled.size() == 2
                           && hello.handled.size() == 1
                           && heavy.handled.size() == 2;
            });
    }



}
