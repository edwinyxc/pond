package com.shuimin.jtiny.codec.session;

import com.shuimin.base.S;

import java.util.HashMap;

/**
 * Created by ed on 2014/4/18.
 */
public class Session extends HashMap<String,Object> {

    private long LAT = S.time();

    private final String id;

    public Session(String id) {
        this.id = id;
    }

    public String id(){
        return id;
    }


    public Session set(String key, Object value){
        this.LAT = S.time();
        this.put(key,value);
        return this;
    }

    public Object get(String key){
        this.LAT = S.time();
        return super.get(key);
    }

    public long lastActiveTime(){
        return this.LAT;
    }
}
