package pond.common.f;

import pond.common.S;

import java.util.Optional;

public class Either<A, B> {
    //e2
    public static <A, B> Either<A, B> of(A a, B b) { return new Either<>(a, b); }
    public static <A, B> Either<A, B> left(A a) { return new Either<>(a, null); }
    public static <A, B> Either<A, B> e2a(A a) { return new Either<>(a, null); }
    public static <A, B> Either<A, B> right(B b) { return new Either<>(null, b); }
    public static <A, B> Either<A, B> e2b(B b) { return new Either<>(null, b); }

    //e3
    public static <A, B, C> E3<A, B, C> of(A a, B b, C c) { return new E3<>(a, b, c); }
    public static <A, B, C> E3<A, B, C> e3a(A a) { return new E3<>(a, null, null); }
    public static <A, B, C> E3<A, B, C> e3b(B b) { return new E3<>(null, b, null); }
    public static <A, B, C> E3<A, B, C> e3c(C c) { return new E3<>(null, null, c); }

    //e4
    public static <A, B, C, D> E4<A, B, C, D> of(A a, B b, C c, D d) { return new E4<>(a, b, c, d); }
    public static <A, B, C, D> E4<A, B, C, D> e4a(A a) { return new E4<>(a, null, null,null); }
    public static <A, B, C, D> E4<A, B, C, D> e4b(B b) { return new E4<>(null, b, null,null); }
    public static <A, B, C, D> E4<A, B, C, D> e4c(C c) { return new E4<>(null, null, c,null); }
    public static <A, B, C, D> E4<A, B, C, D> e4d(D d) { return new E4<>(null, null, null, d); }

    //e5
    public static <A, B, C, D, E> E5<A, B, C, D, E> of(A a, B b, C c, D d, E e) { return new E5<>(a, b, c, d, e); }
    public static <A, B, C, D, E> E5<A, B, C, D, E> e5a(A a) { return new E5<>(a, null, null,null, null); }
    public static <A, B, C, D, E> E5<A, B, C, D, E> e5b(B b) { return new E5<>(null, b, null,null, null); }
    public static <A, B, C, D, E> E5<A, B, C, D, E> e5c(C c) { return new E5<>(null, null, c,null, null); }
    public static <A, B, C, D, E> E5<A, B, C, D, E> e5d(D d) { return new E5<>(null, null, null, d, null); }
    public static <A, B, C, D, E> E5<A, B, C, D, E> e5e(E e) { return new E5<>(null, null, null, null, e); }

    //e6
    public static <A, B, C, D, E, F> E6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f) { return new E6<>(a, b, c, d, e, f); }
    public static <A, B, C, D, E, F> E6<A, B, C, D, E, F> e6a(A a) { return new E6<>(a, null, null,null, null, null); }
    public static <A, B, C, D, E, F> E6<A, B, C, D, E, F> e6b(B b) { return new E6<>(null, b, null,null, null, null); }
    public static <A, B, C, D, E, F> E6<A, B, C, D, E, F> e6c(C c) { return new E6<>(null, null, c,null, null, null); }
    public static <A, B, C, D, E, F> E6<A, B, C, D, E, F> e6d(D d) { return new E6<>(null, null, null, d, null, null); }
    public static <A, B, C, D, E, F> E6<A, B, C, D, E, F> e6e(E e) { return new E6<>(null, null, null, null, e, null); }
    public static <A, B, C, D, E, F> E6<A, B, C, D, E, F> e6f(F f) { return new E6<>(null, null, null, null, null, f); }

    //e7
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> of(A a, B b, C c, D d, E e, F f, G g) { return new E7<>(a, b, c, d, e, f, g); }
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> e7a(A a) { return new E7<>(a, null, null,null, null, null,null); }
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> e7b(B b) { return new E7<>(null, b, null,null, null, null, null); }
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> e7c(C c) { return new E7<>(null, null, c,null, null, null, null); }
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> e7d(D d) { return new E7<>(null, null, null, d, null, null, null); }
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> e7e(E e) { return new E7<>(null, null, null, null, e, null, null); }
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> e7f(F f) { return new E7<>(null, null, null, null, null, f, null); }
    public static <A, B, C, D, E, F, G> E7<A, B, C, D, E, F, G> e7g(G g) { return new E7<>(null, null, null, null, null, null, g); }

    //e8
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> of(A a, B b, C c, D d, E e, F f, G g, H h) { return new E8<>(a, b, c, d, e, f, g, h); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8a(A a) { return new E8<>(a, null, null,null, null, null,null, null); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8b(B b) { return new E8<>(null, b, null,null, null, null, null, null); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8c(C c) { return new E8<>(null, null, c,null, null, null, null, null); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8d(D d) { return new E8<>(null, null, null, d, null, null, null, null); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8e(E e) { return new E8<>(null, null, null, null, e, null, null, null); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8f(F f) { return new E8<>(null, null, null, null, null, f, null, null); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8g(G g) { return new E8<>(null, null, null, null, null, null, g, null); }
    public static <A, B, C, D, E, F, G, H> E8<A, B, C, D, E, F, G, H> e8h(H h) { return new E8<>(null, null, null, null, null, null, null, h); }

    public final Optional<A> _a;
    public final Optional<B> _b;

    Either(A a, B b) {
        _a = Optional.of(a);
        _b = Optional.of(b);
    }

    public void match(Callback<A> caseA, Callback<B> caseB) {
        _a.ifPresent(caseA::apply);
        _b.ifPresent(caseB::apply);
    }

    public static class E3<A, B, C> {
        public final Optional<A> _a;
        public final Optional<B> _b;
        public final Optional<C> _c;

        E3(A a, B b, C c) {
            _a = Optional.of(a);
            _b = Optional.of(b);
            _c = Optional.of(c);
        }

        public void match(Callback<A> caseA, Callback<B> caseB, Callback<C> caseC) {
            _a.ifPresent(caseA::apply);
            _b.ifPresent(caseB::apply);
            _c.ifPresent(caseC::apply);
        }
    }


    public static class E4<A, B, C, D> {
        public final Optional<A> _a;
        public final Optional<B> _b;
        public final Optional<C> _c;
        public final Optional<D> _d;

        E4(A a, B b, C c, D d) {
            _a = Optional.of(a);
            _b = Optional.of(b);
            _c = Optional.of(c);
            _d = Optional.of(d);
        }

        public void match(Callback<A> caseA, Callback<B> caseB, Callback<C> caseC, Callback<D> caseD) {
            _a.ifPresent(caseA::apply);
            _b.ifPresent(caseB::apply);
            _c.ifPresent(caseC::apply);
            _d.ifPresent(caseD::apply);
        }
    }

    public static class E5<A, B, C, D, E> {
        public final Optional<A> _a;
        public final Optional<B> _b;
        public final Optional<C> _c;
        public final Optional<D> _d;
        public final Optional<E> _e;

        E5(A a, B b, C c, D d, E e) {
            _a = Optional.of(a);
            _b = Optional.of(b);
            _c = Optional.of(c);
            _d = Optional.of(d);
            _e = Optional.of(e);
        }

        public void match(Callback<A> caseA, Callback<B> caseB, Callback<C> caseC, Callback<D> caseD, Callback<E> caseE) {
            _a.ifPresent(caseA::apply);
            _b.ifPresent(caseB::apply);
            _c.ifPresent(caseC::apply);
            _d.ifPresent(caseD::apply);
            _e.ifPresent(caseE::apply);
        }
    }

    public static class E6<A, B, C, D, E, F> {
        public final Optional<A> _a;
        public final Optional<B> _b;
        public final Optional<C> _c;
        public final Optional<D> _d;
        public final Optional<E> _e;
        public final Optional<F> _f;

        E6(A a, B b, C c, D d, E e, F f) {
            _a = Optional.of(a);
            _b = Optional.of(b);
            _c = Optional.of(c);
            _d = Optional.of(d);
            _e = Optional.of(e);
            _f = Optional.of(f);
        }

        public void match(Callback<A> caseA,
                          Callback<B> caseB,
                          Callback<C> caseC,
                          Callback<D> caseD,
                          Callback<E> caseE,
                          Callback<F> caseF
        ) {

            _a.ifPresent(caseA::apply);
            _b.ifPresent(caseB::apply);
            _c.ifPresent(caseC::apply);
            _d.ifPresent(caseD::apply);
            _e.ifPresent(caseE::apply);
            _f.ifPresent(caseF::apply);
        }
    }

    public static class E7<A, B, C, D, E, F, G> {
        public final Optional<A> _a;
        public final Optional<B> _b;
        public final Optional<C> _c;
        public final Optional<D> _d;
        public final Optional<E> _e;
        public final Optional<F> _f;
        public final Optional<G> _g;

        E7(A a, B b, C c, D d, E e, F f, G g) {
            _a = Optional.of(a);
            _b = Optional.of(b);
            _c = Optional.of(c);
            _d = Optional.of(d);
            _e = Optional.of(e);
            _f = Optional.of(f);
            _g = Optional.of(g);
        }

        public void match(Callback<A> caseA,
                          Callback<B> caseB,
                          Callback<C> caseC,
                          Callback<D> caseD,
                          Callback<E> caseE,
                          Callback<F> caseF,
                          Callback<G> caseG
        ) {

            _a.ifPresent(caseA::apply);
            _b.ifPresent(caseB::apply);
            _c.ifPresent(caseC::apply);
            _d.ifPresent(caseD::apply);
            _e.ifPresent(caseE::apply);
            _f.ifPresent(caseF::apply);
            _g.ifPresent(caseG::apply);
        }
    }

    public static class E8<A, B, C, D, E, F, G, H> {
        public final Optional<A> _a;
        public final Optional<B> _b;
        public final Optional<C> _c;
        public final Optional<D> _d;
        public final Optional<E> _e;
        public final Optional<F> _f;
        public final Optional<G> _g;
        public final Optional<H> _h;

        E8(A a, B b, C c, D d, E e, F f, G g, H h) {
            _a = Optional.of(a);
            _b = Optional.of(b);
            _c = Optional.of(c);
            _d = Optional.of(d);
            _e = Optional.of(e);
            _f = Optional.of(f);
            _g = Optional.of(g);
            _h = Optional.of(h);
        }

        public void match(Callback<A> caseA,
                          Callback<B> caseB,
                          Callback<C> caseC,
                          Callback<D> caseD,
                          Callback<E> caseE,
                          Callback<F> caseF,
                          Callback<G> caseG,
                          Callback<H> caseH
        ) {

            _a.ifPresent(caseA::apply);
            _b.ifPresent(caseB::apply);
            _c.ifPresent(caseC::apply);
            _d.ifPresent(caseD::apply);
            _e.ifPresent(caseE::apply);
            _f.ifPresent(caseF::apply);
            _g.ifPresent(caseG::apply);
            _h.ifPresent(caseH::apply);
        }
    }
}
