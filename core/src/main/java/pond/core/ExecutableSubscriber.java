package pond.core;

import pond.common.S;
import pond.common.f.Callback;

import java.util.concurrent.Flow;

public class ExecutableSubscriber implements Flow.Subscriber<Executable> {

    final Ctx ctx;
    private Flow.Subscription subscription;
    Callback.C2<Ctx, Executable> handler;
    public ExecutableSubscriber(Ctx ctx, Callback.C2<Ctx, Executable> handler) {
        this.ctx = ctx;
        this.handler = handler;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Executable item) {
        S.echo("Got", item);
        handler.apply(ctx, item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        ctx.error(throwable);
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        subscription.cancel();//cancel
    }

    public static ExecutableSubscriber run(Ctx ctx){
        return new ExecutableSubscriber(ctx, (c, exec)->{
            c.push(exec);
            var e = c.next();
            assert e != null;
            e.body().apply(c);
        });
    }
}
