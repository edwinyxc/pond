package pond.common.f;


public class Tuple<A, B> {
  public final A _a;
  public final B _b;

  @Override
  public String toString() {
    return "<" + _a + "," + _b + ">";
  }

  protected Tuple(A a, B b) {
    _a = a;
    _b = b;
  }

  public <PA, PB> Tuple<A, B> tap(Function<PA, A> pa, Function<PB, B> pb) {
    pa.apply(this._a);
    pb.apply(this._b);
    return this;
  }

  public <R> Tuple<A, B> tap(Callback.C2<A, B> fn) {
    fn.apply(this._a, this._b);
    return this;
  }

  public <PA, PB> Tuple<PA, PB> product(Function<PA, A> pa, Function<PB, B> pb) {
    return pair(pa.apply(this._a), pb.apply(this._b));
  }

  public static <A, B> Tuple<A, B> pair(A a, B b) {
    return new Tuple<A, B>(a, b);
  }

  public static <A, B> Tuple<A, B> t2(A a, B b) {
    return pair(a, b);
  }

  public static <A, B, C> T3<A, B, C> pair(A a, B b, C c) {
    return new T3<>(a, b, c);
  }

  public static <A, B, C> T3<A, B, C> t3(A a, B b, C c) {
    return new T3<>(a, b, c);
  }

  public static <A, B, C, D> T4<A, B, C, D> pair(A a, B b, C c, D d) {
    return new T4<>(a, b, c, d);
  }

  public static <A, B, C, D> T4<A, B, C, D> t4(A a, B b, C c, D d) {
    return new T4<>(a, b, c, d);
  }

  public static <A, B, C, D, E> T5<A, B, C, D, E> pair(A a, B b, C c,
                                                       D d, E e) {
    return new T5<>(a, b, c, d, e);
  }

  public static <A, B, C, D, E> T5<A, B, C, D, E> t5(A a, B b, C c,
                                                     D d, E e) {
    return new T5<>(a, b, c, d, e);
  }

  public static class T3<A, B, C> {
    public final A _a;
    public final B _b;
    public final C _c;

    protected T3(A a, B b, C c) {
      _a = a;
      _b = b;
      _c = c;
    }

    @Override
    public String toString() {
      return "<" + _a + "," + _b + "," + _c + ">";
    }

    public <PA, PB, PC> T3<A, B, C> tap(
        Function<PA, A> pa,
        Function<PB, B> pb,
        Function<PC, C> pc
    ) {
      pa.apply(this._a);
      pb.apply(this._b);
      pc.apply(this._c);
      return this;
    }

    public <R> T3<A, B, C> tap(Callback.C3<A, B, C> fn) {
      fn.apply(this._a, this._b, this._c);
      return this;
    }

    public <PA, PB, PC> T3<PA, PB, PC> product(
        Function<PA, A> pa,
        Function<PB, B> pb,
        Function<PC, C> pc
    ) {
      return pair(
          pa.apply(this._a),
          pb.apply(this._b),
          pc.apply(this._c)
      );
    }
  }

  public static class T4<A, B, C, D> {
    public final A _a;
    public final B _b;
    public final C _c;
    public final D _d;

    protected T4(A a, B b, C c, D d) {
      _a = a;
      _b = b;
      _c = c;
      _d = d;
    }

    @Override
    public String toString() {
      return "<" + _a + "," + _b + "," + _c + "," + _d + ">";
    }

    public <PA, PB, PC, PD> T4<A, B, C, D> tap(
        Function<PA, A> pa,
        Function<PB, B> pb,
        Function<PC, C> pc,
        Function<PD, D> pd
    ) {
      pa.apply(this._a);
      pb.apply(this._b);
      pc.apply(this._c);
      pd.apply(this._d);
      return this;
    }

    public <R> T4<A, B, C, D> tap(Callback.C4<A, B, C, D> fn) {
      fn.apply(this._a, this._b, this._c, this._d);
      return this;
    }

    public <PA, PB, PC, PD> T4<PA, PB, PC, PD> product(
        Function<PA, A> pa,
        Function<PB, B> pb,
        Function<PC, C> pc,
        Function<PD, D> pd
    ) {
      return pair(
          pa.apply(this._a),
          pb.apply(this._b),
          pc.apply(this._c),
          pd.apply(this._d)
      );
    }
  }

  public static class T5<A, B, C, D, E> {
    public final A _a;
    public final B _b;
    public final C _c;
    public final D _d;
    public final E _e;

    protected T5(A a, B b, C c, D d, E e) {
      _a = a;
      _b = b;
      _c = c;
      _d = d;
      _e = e;
    }

    public <R> T5<A, B, C, D, E> tap(Callback.C5<A, B, C, D, E> fn) {
      fn.apply(this._a, this._b, this._c, this._d, this._e);
      return this;
    }

    public <PA, PB, PC, PD, PE> T5<A, B, C, D, E> tap(
        Function<PA, A> pa,
        Function<PB, B> pb,
        Function<PC, C> pc,
        Function<PD, D> pd,
        Function<PE, E> pe
    ) {
      pa.apply(this._a);
      pb.apply(this._b);
      pc.apply(this._c);
      pd.apply(this._d);
      pe.apply(this._e);
      return this;
    }

    public <PA, PB, PC, PD, PE> T5<PA, PB, PC, PD, PE> product(
        Function<PA, A> pa,
        Function<PB, B> pb,
        Function<PC, C> pc,
        Function<PD, D> pd,
        Function<PE, E> pe
    ) {
      return pair(
          pa.apply(this._a),
          pb.apply(this._b),
          pc.apply(this._c),
          pd.apply(this._d),
          pe.apply(this._e)
      );
    }

    @Override
    public String toString() {
      return "<" + _a + "," + _b + "," + _c + "," + _d + "," + _e + ">";
    }
  }
}
