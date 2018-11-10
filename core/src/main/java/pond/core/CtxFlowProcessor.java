package pond.core;

import pond.common.S;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;


public class CtxFlowProcessor extends SubmissionPublisher<Ctx> implements java.util.concurrent.Flow.Processor<Ctx, Ctx>{

    private java.util.concurrent.Flow.Subscription subscription;
    public final List<Executable> handled = new LinkedList<>();
    public CtxFlowProcessor(){
        super();
    }

    @Override
    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
        S.echo("OnSubscribe");
        if(this.getSubscribers().contains(this)){
            throw new IllegalArgumentException("Can not subscribe on self");
        }
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Ctx c) {
        S.echo(this.toString() + " Got ctx Next: " + c.peek());
        for(Executable exec = c.peek(); exec != null; ) {
            if(exec instanceof Executable.Flow
                   && ((Executable.Flow) exec).targetSubscriber() != this
            ){
                //rx
                Flow.Subscriber<Ctx> target = ((Executable.Flow) exec).targetSubscriber();
                S.echo("target" , target);

                if(!this.getSubscribers().contains(target)){
                    this.subscribe(target);
                }
                S.echo("submit to " + target);
                submit(c);
                //submit and wait the message
                subscription.request(1);
                return;
            }
            else {
                exec = c.next(); //move to next
                if(exec != null){
                    S.echo(this.toString() + " Run exec: " + exec.name() + " on " + Thread.currentThread());
                    exec.body().apply(c);
                    handled.add(exec);
                }
            }
        }

        //we only process a single Ctx here
        subscription.cancel();
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        // dont close
    }
}
