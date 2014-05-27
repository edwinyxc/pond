package com.shuimin.szgwyw;

import com.shuimin.pond.codec.db.Record;
import com.shuimin.pond.core.mw.Action;

import java.util.ArrayList;

/**
 * Created by ed on 2014/5/8.
 */
public class List {

    public Action list(String category) {
           return Action.simple((req,resp) -> {
               java.util.List<Record> list = new ArrayList<>();

           });
    }
}
