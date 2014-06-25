package com.shuimin.common.abs;

import java.util.Map;

public interface Attrs<T> {

    public  T attr(String name, Object o);

    public  Object attr(String name) ;

    public  Map<String,Object> attrs() ;

}

