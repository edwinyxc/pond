package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.f.Callback;

public interface Executable<T extends Ctx> extends Callback<T> {

    Logger logger = LoggerFactory.getLogger(Executable.class);

    interface Flow<T extends Ctx> extends Executable<T> {
        CtxFlowProcessor targetSubscriber();
    }


    default Executable<T> flowTo(CtxFlowProcessor target) {
        var _this = this;
        return new Flow<>() {
            @Override
            public void apply(T t) {
                _this.apply(t);
            }

            @Override
            public CtxFlowProcessor targetSubscriber() {
                return target;
            }

            @Override
            public String toString(){
                return "Flow["+hashCode()+ "::=>" + target.name() + "]";
            }

        };
    }

    static <T extends Ctx> Executable<T> of(Callback<T> callback){
        return callback::apply;
    }

}
