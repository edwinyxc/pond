package pond.common.struc;

import pond.common.S;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<E> implements Iterator<E> {
    final Enumeration<E> enumeration;

    protected EnumerationIterator(Enumeration<E> e) {
        enumeration = e;
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    @Override
    public E next() {
        return enumeration.nextElement();
    }

    @Override
    public void remove() {
        S._fail("Not supported.");
    }

}
