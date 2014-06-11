package com.shuimin.common.abs;

import java.util.HashMap;
import java.util.Map;

public interface Attrs<T> {
    Map<String, Object> attrs = new HashMap<>();

    @SuppressWarnings("unchecked")
    public default T attr(String name, Object o) {
        attrs.put(name, o);
        return (T) this;
    }

    public default Object attr(String name) {
        return attrs.get(name);
    }

}
