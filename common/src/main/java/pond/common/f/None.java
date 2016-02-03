package pond.common.f;

import java.util.Collections;
import java.util.Iterator;

public class None<T> extends Option<T> {

  @Override
  public Iterator<T> iterator() {
    return Collections.<T>emptyList().iterator();
  }

  @Override
  public boolean isPresent() {
    return false;
  }

  @Override
  public T get() {
    throw new IllegalStateException("access none value");
  }

  @Override
  public String toString() {
    return "no value";
  }

}
