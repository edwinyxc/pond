package pond.common.f;

import java.util.Collections;
import java.util.Iterator;

public class Some<T> extends Option<T> {

  final T value;
  final Iterator<T> singletonIterator;

  public Some(T value) {
    this.value = value;
    this.singletonIterator= Collections.singletonList(value).iterator();
  }

  public Iterator<T> iterator() {
    return singletonIterator;
  }

  @Override
  public String toString() {
    return "Some(" + value + ")";
  }

  @Override
  public boolean isPresent() {
    return true;
  }

  @Override
  public T get() {
    return value;
  }

}
