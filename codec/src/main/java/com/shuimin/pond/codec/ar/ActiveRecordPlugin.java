package com.shuimin.pond.codec.ar;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.mw.Plugin;

/**
 * Created by ed on 2001/1/1.
 */
public class ActiveRecordPlugin extends Plugin {
    protected String keyId = "vid" ;
    protected Function._0<String> keyProvider = S.uuid::vid;

    public ActiveRecordPlugin keyId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    public ActiveRecordPlugin keyProvider (Function._0<String> provider) {
        this.keyProvider = provider;
        return this;
    }

    @Override
    public void install() {
    }
}
