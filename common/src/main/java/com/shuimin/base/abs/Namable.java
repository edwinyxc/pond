package com.shuimin.base.abs;

public interface Namable<T>
{
	public String name();

	public T name(String name);
}
