package com.shuimin.common.struc;

import java.util.Iterator;

public class NullIterator<E> implements Iterator<E>
{

	@Override
	public boolean hasNext()
	{
		return false;
	}

	@Override
	public E next()
	{
		return null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("remove not supported, yet.");
	}

}
