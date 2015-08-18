package pond.common.struc;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NullIterator<E> implements Iterator<E> {

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public E next() {
    throw new NoSuchElementException("null iterator");
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove not supported, yet.");
  }

}
