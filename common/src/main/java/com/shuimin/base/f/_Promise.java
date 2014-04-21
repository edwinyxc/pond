package com.shuimin.base.f;

import java.util.List;

public class _Promise<T> {
	
	int _state = -1;//default-non-existed err code ,should never occur
	
	public static int STATE_PENDING = 0;
	public static int STATE_FULFILLED = 1;
	public static int STATE_REJECTED = 2;
	
	Exception reson;//exception to be throw
	
	T _value;
	
	List<Function<?,T>> _onFulfilleds;
	
	List<Function<?,T>> _onRejecteds;
	
	_Promise<?> nextPromise;
	
	
	public <X> _Promise<X> then(Function<X, T> onFulfilled,
	
								Function<X,T> onRejected){
		return null;
	}
	
	public static <X> void resolve(_Promise<?> promise ,X x){
		
	}
	
	
	
	
}
