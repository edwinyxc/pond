package com.shuimin.base.f;

public interface Callback<A> {

    public void apply(A t);

    public interface _0 {

        public void apply();
    }

    public interface _2< A, B> {

        public void apply(A a, B b);
    }

    public interface _3< A, B, C> {

        public void apply(A a, B b, C c);
    }

    public interface _4< A, B, C, D> {

        public void apply(A a, B b, C c, D d);
    }

    public interface _5< A, B, C, D, E> {

        public void apply(A a, B b, C c, D d, E e);
    }

}
