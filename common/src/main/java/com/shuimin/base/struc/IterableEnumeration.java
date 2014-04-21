package com.shuimin.base.struc;

import java.util.Enumeration;
import java.util.Iterator;

public class IterableEnumeration<E> implements Iterable<E> {

	final Enumeration<E> enumeration;

	public IterableEnumeration(Enumeration<E> e) {
		enumeration = e;
	}

	@Override
	public Iterator<E> iterator() {
		return new EnumerationIterator<E>(enumeration);
	}

}
