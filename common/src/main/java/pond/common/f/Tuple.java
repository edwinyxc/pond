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

  public static <A, B> Tuple<A, B> pair(A a, B b) {
    return new Tuple<A, B>(a, b);
  }

  public static <A, B> Tuple<A, B> t2(A a, B b) {
    return pair(a, b);
  }

  public static <A, B, C> T3<A, B, C> t3(A a, B b, C c) {
    return new T3<A, B, C>(a, b, c);
  }

  public static <A, B, C, D> T4<A, B, C, D> t4(A a, B b, C c, D d) {
    return new T4<A, B, C, D>(a, b, c, d);
  }

  public static <A, B, C, D, E> T5<A, B, C, D, E> t5(A a, B b, C c,
                                                     D d, E e) {
    return new T5<A, B, C, D, E>(a, b, c, d, e);
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

    @Override
    public String toString() {
      return "<" + _a + "," + _b + "," + _c + "," + _d + "," + _e + ">";
    }
  }
}
