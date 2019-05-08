package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.SubmissionPublisher;


/**
 * A Reactive Processor bound ta an Executor,
 * Taking Ctx and dispatch to the next
 * @see Ctx#runReactiveFlow(Ctx.ReactiveFlowConfig)
 */
public class CtxFlowProcessor extends SubmissionPublisher<Ctx> implements java.util.concurrent.Flow.Processor<Ctx, Ctx>{
    public static Logger LOG = LoggerFactory.getLogger(CtxFlowProcessor.class);

    private java.util.concurrent.Flow.Subscription subscription;
    public final List<CtxHandler> handled = new LinkedList<>();
    private String name;
    private Executor executor = null;
    private Callback<Throwable> onError = Throwable::printStackTrace;
    private CtxFlowProcessor last;
    private Callback<Ctx> onFinal = ctx -> {
        LOG.trace("Last but not least !!! Calling Ctx#flowProcessor");
        if(last != null || (last = ctx.flowProcessor()) != null){
            LOG.trace(String.format("null last obj: %s", S.dump(last)));
            last.onFinal.apply(ctx);
        }
    };

    public CtxFlowProcessor(String name){
        super();
        this.name = name;
    }

    public CtxFlowProcessor(){
        super();
        this.name = this.toString();
    }

    public CtxFlowProcessor errorHandler(Callback<Throwable> onError){
        this.onError = onError;
        return this;
    }

    public CtxFlowProcessor finalHandler(CtxHandler<Ctx> onFinal) {
        this.onFinal = onFinal;
        return this;
    }

    public String name(){
        return name;
    }

    public CtxFlowProcessor executor(Executor executor){
        this.executor = executor;
        return this;
    }


    @Override
    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
        Ctx.logger.trace("FLOW(" + this.name + ")------------");
        if(this.getSubscribers().contains(this)){
            throw new IllegalArgumentException("Can not subscribe on self");
        }
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNext(Ctx c) {
        Ctx.logger.trace("FLOW("+this.name()+ ")<<<" + c.current() + " On " + Thread.currentThread());
        CtxHandler exec = c.current();
        if(exec != null) {
            if(exec instanceof CtxHandler.Flow ){
                if(((CtxHandler.Flow) exec).targetSubscriber() != this){
                    last = ((CtxHandler.Flow) exec).targetSubscriber();
                    //rx
                    CtxFlowProcessor target = ((CtxHandler.Flow) exec).targetSubscriber();
                    //prevent ring publishing
                    subscription.cancel();
                    if(!this.getSubscribers().contains(target)){
                        this.subscribe(target);
                    }
                    Ctx.logger.trace("FLOW(" + this.name()+")>>>" + target.name());
                    submit(c);
                    //submit and wait the message
                    subscription.request(1);
                    return;
                }
                else {
                    if(executor == null){ //sync mode
                        Ctx.logger.trace("FLOW("+this.name()+")=||" + exec + " On " + Thread.currentThread() );
                        try {
                            exec.apply(c);
                            handled.add(exec);
                        }catch (Throwable th){
                            onError.apply(th);
                        }finally {
                            if(c.next() != null) onNext(c);
                            else onFinal.apply(c);
                        }
                    }else {
                        CompletableFuture.supplyAsync(() -> {
                            Ctx.logger.trace("FLOW(" + this.name() + ")~||" + exec + " On " + Thread.currentThread());
                            exec.apply(c);
                            handled.add(exec);
                            return true;
                        }, executor).handle((suc, ex) ->{
                            if(suc){
                                if(c.next() != null) onNext(c);
                                else onFinal.apply(c);
                                return true;
                            } else {
                                onError.apply(ex);
                                return false;
                            }
                        });
                    }
                }
            }
            else {
                Ctx.logger.warn("FLOW("+this.name()+")UNEXPECTED!!");
                Ctx.logger.warn("FLOW("+this.name()+")==>" + exec + " On " + Thread.currentThread() );
                exec.apply(c);
                handled.add(exec);
                if(c.next() != null) onNext(c);
                else onFinal.apply(c);
            }
        }
        else {
            onFinal.apply(c);
            close();
        }
        //we only process a single Ctx here
        //subscription.cancel();
    }

    @Override
    public void onError(Throwable throwable) {
        onError.apply(throwable);
    }

    @Override
    public void onComplete() {
        subscription.cancel();
        // dont close
    }
}
