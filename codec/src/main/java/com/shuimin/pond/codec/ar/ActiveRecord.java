package com.shuimin.pond.codec.ar;

import com.shuimin.pond.codec.db.Record;

import java.util.Set;

/**
 * Created by ed on 2014/4/30.
 */
public class ActiveRecord {
    final Record d;
    public ActiveRecord(Record r) {
        this.d = r;
    }
    private Table table;

    public Set<String> colNames() {
        return d.fields();
    }

    public ActiveRecord save(){
        table.update(this);
        return this;
    }

    public void delete(){
        table.delete(this);
    }

    public ActiveRecord update(Object[]... args){

        for(Object[] pair : args) {
            if(pair[0] != null)
            d.set(pair[0].toString(), pair[1]);
        }
        return save();
    }




}
