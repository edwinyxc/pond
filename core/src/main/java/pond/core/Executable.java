package pond.core;

import pond.common.f.Callback;

public interface Executable {
    String name();
    Callback<Ctx> body();

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

    interface InterceptorPre extends Executable{
        //TODO
    }

    interface InterceptorPost extends Executable{
        //TODO
    }
}
