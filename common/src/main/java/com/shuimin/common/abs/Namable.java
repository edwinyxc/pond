package com.shuimin.common.abs;

public interface Namable<T>
{
	public String name();

	public T name(String name);
}
