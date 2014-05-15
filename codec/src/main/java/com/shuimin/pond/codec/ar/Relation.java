package com.shuimin.pond.codec.ar;


import com.shuimin.pond.core.Pond;

import java.util.Map;

/**
 * Created by ed on 2014/4/30.
 */
public class Relation {
    final Map<String, Relation> relations;
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
        this.key = name.concat("_"
            + ((ActiveRecordPlugin)Pond.register(ActiveRecordPlugin.class)).keyId);

        this.relation = null;
    }

    public boolean isOnlyOne() {
        return onlyOne;
    }

    public boolean isAncestor() {
        return ancestor;
    }

    public boolean isCross() {
        return relation != null;
    }

    public Relation by(String key) {
        this.key = key;
        return this;
    }

    public Relation in(String table) {
        this.target = table;
        return this;
    }

    public Relation through(String rel) {
        if (relations.containsKey(rel)) {
            this.relation = relations.get(rel);
        } else {
            throw new RuntimeException("undefined relation: " + rel);
        }
        return this;
    }

    String assoc(String table, String id) {
        String idMark = ((ActiveRecordPlugin)Pond.register(ActiveRecordPlugin.class)).keyId;
        String tmpl = isAncestor() ?
            "%1$s on %2$s.%3$s = %1$s." + idMark
            :
            "%1$s on %1$s.%3$s = %2$s." + idMark;

        return isCross() ?
            String.format(tmpl, relation.target, target, key)
                .concat(" JOIN ").concat(relation.assoc(table, id))
            :
            String.format(tmpl.concat(" AND %1$s.id = '%4$s' "));
    }



}
