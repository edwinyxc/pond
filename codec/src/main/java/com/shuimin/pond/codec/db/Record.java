package com.shuimin.pond.codec.db;


import com.shuimin.common.util.logger.Logger;

import java.util.HashMap;

/**
 * <p>数据记录抽象</p>
 */
public class Record extends HashMap<String,Object> {

    public static Logger logger = Logger.get();

    private String tableName;

    public String table() {
        return tableName;
    }

    public Record table(String name) {
        this.tableName = name;
        return this;
    }

    @Override
    public Record put(String key, Object val) {
        super.put(key,val);
        return this;
    }

}
