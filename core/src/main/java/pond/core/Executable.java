package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.f.Callback;

public interface Executable {

    Logger logger = LoggerFactory.getLogger(Executable.class);
    String name();
    Callback<Ctx> body();


    interface Flow extends Executable{
        java.util.concurrent.Flow.Subscriber<Ctx> targetSubscriber();
    }


    default Executable flowTo(java.util.concurrent.Flow.Subscriber<Ctx> target) {
        var _this = this;
        return new Flow() {
            @Override
            public java.util.concurrent.Flow.Subscriber<Ctx> targetSubscriber() {
                return target;
            }

            @Override
            public String name() {
                return _this.name();
            }

            @Override
            public Callback<Ctx> body() {
                return _this.body();
            }

        };
    }

    static Executable of(String name, Callback<Ctx> callback){
        return new Executable() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Callback<Ctx> body() {
                return callback;
            }

            @Override
            public String toString() {
                return "name " + name + " : " + callback.hashCode();
            }

        };
    }

}
