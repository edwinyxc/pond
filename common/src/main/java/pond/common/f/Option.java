package pond.common.f;

public abstract class Option<T> implements Iterable<T> {
  public final static None<Object> _none = new None<>();

  @SuppressWarnings("unchecked")
  public static <T> None<T> none() {
    return (None<T>) _none;
  }

  public static <T> Some<T> some(T t) {
    return new Some<T>(t);
  }

  public abstract boolean isPresent();


  public abstract T get();


  public T getOrElse(T _else){
    return isPresent()? get():_else;
  }

}
