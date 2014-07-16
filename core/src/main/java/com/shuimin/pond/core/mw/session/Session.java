package com.shuimin.pond.core.mw.session;

import com.shuimin.pond.core.CtxExec;
import com.shuimin.pond.core.Request;

/**
 * Created by ed on 2014/4/18.
 */
public interface Session {

    public String id();

    public Object get(String key);

    public Session set(String key, Object value);

    public static Session get(){
        return SessionManager.get(CtxExec.get());
    }
}
