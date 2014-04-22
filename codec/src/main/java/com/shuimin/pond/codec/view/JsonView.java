package com.shuimin.pond.codec.view;

import com.alibaba.fastjson.JSON;
import com.shuimin.common.S;

import java.util.Map;

public class JsonView extends TextView {

    protected JsonView(Map map) {
        super(JSON.toJSONString(S._notNull(map)));
    }

    protected JsonView() {
        super("");
    }

    public static JsonView one(Map map) {
        return new JsonView(map);
    }

    public JsonView of(Map map) {
        return new JsonView(map);
    }
}
