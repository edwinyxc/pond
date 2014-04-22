package com.shuimin.pond.core.misc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 2014/4/11.
 */
public interface Attrs<H> {
    final Map<String,Object> attrs = new HashMap<>();
    public default <T> T attr(String s){
        return (T) attrs.get(s);
    }

    public default H attr(String s,Object o){
        attrs.put(s,o);
        return (H)this;
    }
}
