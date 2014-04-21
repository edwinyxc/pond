package com.shuimin.base.f;

public interface Function<R, A> {

    public R apply(A t);

    public interface _0<R> {

        public R apply();
    }

    public interface _2<R, A, B> {

        public R apply(A a, B b);
    }

    public interface _3<R, A, B, C> {

        public R apply(A a, B b, C c);
    }

    public interface _4<R, A, B, C, D> {

        public R apply(A a, B b, C c, D d);
    }

    public interface _5<R, A, B, C, D, E> {

        public R apply(A a, B b, C c, D d, E e);
    }

}
