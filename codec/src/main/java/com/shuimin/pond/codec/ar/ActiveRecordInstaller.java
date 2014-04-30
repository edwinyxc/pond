package com.shuimin.pond.codec.ar;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.AbstractMiddleware;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Server;

/**
 * Created by ed on 2014/4/30.
 */
public class ActiveRecordInstaller extends AbstractMiddleware {

    private String keyIdentifier = "vid";
    private Function._0<String> keyProvider = () -> S.uuid.vid();

    public static final String KEY_IDENTIFIER = "key_identifier";
    public static final String KEY_IDENTIFIER = "key_identifier";

    public ActiveRecordInstaller keyId(String s) {
        this.keyIdentifier = s;
        return this;
    }

    public ActiveRecordInstaller key(Function._0<String> fn) {
        this.
    }

    @Override
    public void init() {
        Server.config(this.getClass(), KEY_IDENTIFIER, keyIdentifier);
    }

    @Override
    public ExecutionContext handle(ExecutionContext ctx) {
        return ctx;
    }
}
