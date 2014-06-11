package com.shuimin.pond.core.spi.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.shuimin.pond.core.spi.JsonService;

/**
 * Created by ed on 14-5-21.
 */
public class AlibabaJson implements JsonService {

    @Override
    public String toString(Object o) {
        return JSON.toJSONString(o, SerializerFeature.DisableCircularReferenceDetect);
    }

    @Override
    public <E> E fromString(Class<E> clazz, String s) {
        return JSON.parseObject(s, new TypeReference<E>() {
        });
    }
}
