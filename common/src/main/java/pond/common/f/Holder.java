package pond.common.f;

/**
 * A final holder for non-final value
 * @Deprecated use Tuple.unit instead
 */
@Deprecated public class Holder<T> {
  protected T val;

  public Holder<T> init(T t) {
    this.val = t;
    return this;
  }

  public T val() {
    return val;
  }

  public Holder<T> val(T t) {
    val = t;
    return this;
  }

}
