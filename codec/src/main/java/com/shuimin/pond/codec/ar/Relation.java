package com.shuimin.pond.codec.ar;

import java.util.Map;

/**
 * Created by ed on 2014/4/30.
 */
public class Relation {
    final Map<String,Relation> relations;
    final boolean onlyOne;
    final boolean ancestor;

    Relation relation;
    String target;
    String key;

    public Relation(Map<String, Relation> relations, String name, boolean onlyOne, boolean ancestor) {
        this.relations = relations;
        this.onlyOne = onlyOne;
        this.ancestor = ancestor;

        this.target = name;
        this.key = name.concat("_"+)
    }

}
