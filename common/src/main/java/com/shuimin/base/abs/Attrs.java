package com.shuimin.base.abs;

public interface Attrs<T>
{
	public T attr(String name, Object o);
	public Object attr(String name);
}
