package com.shuimin.pond.codec.ar;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.mw.Plugin;

import java.util.HashSet;
import java.util.Set;

import static com.shuimin.common.S._assert;
import static com.shuimin.common.S._for;

/**
 * Created by ed on 2001/1/1.
 */
public class ActiveRecordPlugin extends Plugin {
    protected String keyId = "vid" ;
    protected Function._0<String> keyProvider = S.uuid::vid;

    protected Set<String> defaultFields = new HashSet<String>(){
        {
            this.add("create_time timestamp");
            this.add("update_time timestamp");
            this.add("creator varchar(50)");
            this.add("updater varchar(50)");
        }
    };

    protected Callback<String> beforeTableCreation = table -> {};
    protected Callback<Table> afterTableCreation = table -> {};

    protected Callback<String> beforeTableDropping = table -> {};
    protected Callback<String> afterTableDropping = table -> {};

    public ActiveRecordPlugin keyId(String keyId) {
        this.keyId = keyId;
        return this;
    }


    /**
     * <p>pure sql ${name} ${sql type}</p>
     * <pre> create_time timestamp</pre>
     * @param f field sqls
     * @return this
     */
    public ActiveRecordPlugin defaultFields(String... f){
        _assert(f);
        _for(f).each(defaultFields::add);
        return this;
    }

    public ActiveRecordPlugin keyProvider (Function._0<String> provider) {
        _assert(provider);
        this.keyProvider = provider;
        return this;
    }

    @Override
    public void install() {
    }
}
