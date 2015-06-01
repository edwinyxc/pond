package pond.common.struc;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterable<E> implements Iterable<E> {

    final Enumeration<E> enumeration;

    public EnumerationIterable(Enumeration<E> e) {
        enumeration = e;
    }

    @Override
    public Iterator<E> iterator() {
        return new EnumerationIterator<E>(enumeration);
    }

}
