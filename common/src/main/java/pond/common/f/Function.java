package pond.common.f;

public interface Function<R, A> {

  /**
   * Function do nothing but return the argument as-is.
   */
  static Function EMPTY = a -> a;

  static Function NUL = a -> null;

  @SuppressWarnings("unchecked")
  static <R, A> Function<R, A> empty() {
    return EMPTY;
  }

  @SuppressWarnings("unchecked")
  static <R, A> Function<R, A> nul() {
    return NUL;
  }

  R apply(A t);

  interface F0<R> {

    R apply();
  }

  interface F0ERR<R> {

    R apply() throws Exception;
  }

  interface F2<R, A, B> {

    R apply(A a, B b);
  }

  interface F3<R, A, B, C> {

    R apply(A a, B b, C c);
  }

  interface F4<R, A, B, C, D> {

    R apply(A a, B b, C c, D d);
  }

  interface F5<R, A, B, C, D, E> {

    R apply(A a, B b, C c, D d, E e);
  }

}
