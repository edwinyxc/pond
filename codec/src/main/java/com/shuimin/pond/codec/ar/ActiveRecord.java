package com.shuimin.pond.codec.ar;

import com.shuimin.pond.codec.db.CompoundRecord;

import java.util.Set;

/**
 * Created by ed on 2014/4/30.
 */
public class ActiveRecord extends CompoundRecord{

    private Table table;

    public Set<String> colNames() {
        return fields();
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
            set(pair[0].toString(), pair[1]);
        }
        return save();
    }




}
