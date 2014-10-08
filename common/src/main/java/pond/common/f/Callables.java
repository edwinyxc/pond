package pond.common.f;

import java.util.concurrent.Callable;

public abstract class Callables {
    public static <X, A> Callable<X> adapt(final Function<X, A> func, final A a) {
        return new Callable<X>() {

            @Override
            public X call() throws Exception {
                return func.apply(a);
            }

        };
    }

    public static <X, A> Callable<X> adapt_0(final Function.F0<X> func) {
        return new Callable<X>() {

            @Override
            public X call() throws Exception {
                return func.apply();
            }

        };
    }

}
