package pond.core;

import pond.common.S;
import pond.common.f.Callback;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;


public class CtxFlowProcessor extends SubmissionPublisher<Ctx> implements java.util.concurrent.Flow.Processor<Ctx, Ctx>{

    private java.util.concurrent.Flow.Subscription subscription;
    public final List<Executable> handled = new LinkedList<>();
    private String name;

    public CtxFlowProcessor(String name){
        super();
        this.name = name;
    }

    public CtxFlowProcessor(){
        super();
        this.name = this.toString();
    }

    public String name(){
        return name;
    }


    @Override
    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
        S.echo("FLOW(" + this.name + ")------------");
        if(this.getSubscribers().contains(this)){
            throw new IllegalArgumentException("Can not subscribe on self");
        }
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNext(Ctx c) {
        S.echo("FLOW("+this.name()+ ")<<<" + c.current() + " On " + Thread.currentThread());
        Executable exec = c.current();
        while (exec != null) {
            if(exec instanceof Executable.Flow ){
                if(((Executable.Flow) exec).targetSubscriber() != this){
                    //rx
                    CtxFlowProcessor target = ((Executable.Flow) exec).targetSubscriber();
                    //prevent ring publishing
                    subscription.cancel();
                    if(!this.getSubscribers().contains(target)){
                        this.subscribe(target);
                    }
                    S.echo("FLOW(" + this.name()+")>>>" + target.name());
                    submit(c);
                    //submit and wait the message
                    subscription.request(1);
                    return;
                }
                else {
                    S.echo("FLOW("+this.name()+")=||" + exec + " On " + Thread.currentThread() );
                    exec.apply(c);
                    handled.add(exec);
                    exec = c.next();
                    ; //move to next
                }
            }
            else {
                S.echo("FLOW("+this.name()+")UNEXPECTED!!");
                S.echo("FLOW("+this.name()+")==>" + exec + " On " + Thread.currentThread() );
                exec.apply(c);
                handled.add(exec);
                exec = c.next(); //move to next
            }
        }
        //close();
        //we only process a single Ctx here
        //subscription.cancel();
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        subscription.cancel();
        // dont close
    }
}
