package pond.common.f;

import java.util.concurrent.ExecutionException;

public interface Callback<A> {

    public void apply(A t);

    public interface C0 {

        public void apply();
    }

    public interface C0ERR {

        public void apply() throws Exception;
    }

    public interface C2<A, B> {

        public void apply(A a, B b);
    }

    public interface C3<A, B, C> {

        public void apply(A a, B b, C c);
    }

    public interface C4<A, B, C, D> {

        public void apply(A a, B b, C c, D d);
    }

    public interface C5<A, B, C, D, E> {

        public void apply(A a, B b, C c, D d, E e);
    }

}
