package com.shuimin.base.struc;

import java.util.Enumeration;
import java.util.Iterator;

import com.shuimin.base.S;

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
