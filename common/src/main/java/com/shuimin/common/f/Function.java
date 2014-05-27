package com.shuimin.common.f;

public interface Function<R, A> {

    public R apply(A t);

    public interface F0<R> {

        public R apply();
    }

    public interface F2<R, A, B> {

        public R apply(A a, B b);
    }

    public interface F3<R, A, B, C> {

        public R apply(A a, B b, C c);
    }

    public interface F4<R, A, B, C, D> {

        public R apply(A a, B b, C c, D d);
    }

    public interface F5<R, A, B, C, D, E> {

        public R apply(A a, B b, C c, D d, E e);
    }

}
