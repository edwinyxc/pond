package pond.common.f;

@SuppressWarnings("all")
public interface Callback<A> {

  Callback NOOP = a -> {};

  static <A> Callback<A> noop() {
    return NOOP;
  }

  void apply(A t);

  interface C0 {

    void apply();
  }

  interface C0ERR {

    void apply() throws Exception;
  }

  interface C2<A, B> {

    void apply(A a, B b);
  }

  interface C3<A, B, C> {

    void apply(A a, B b, C c);
  }

  interface C4<A, B, C, D> {

    void apply(A a, B b, C c, D d);
  }

  interface C5<A, B, C, D, E> {

    void apply(A a, B b, C c, D d, E e);
  }

}
