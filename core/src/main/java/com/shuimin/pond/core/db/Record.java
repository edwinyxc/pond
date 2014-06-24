package com.shuimin.pond.core.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.common.f.Holder;
import com.shuimin.pond.core.Request;

import java.util.Map;
import java.util.Set;

/**
 * Created by ed on 2014/4/24.
 * <pre>
 * Record represents record from DB,
 * normally, a row from a table can be mapped into a
 * record using JDBCTmpl#map
 * </pre>
 */
public interface Record extends Map<String, Object> {

    final static String DEFAULT_PRI_KEY = "id";

    /**
     * default pk_provider
     */
    static Holder<Function.F0<Object>> PK_PROVIDER =
            new Holder<Function.F0<Object>>().init(S.uuid::vid);

    static void primaryKeySupplier(Function.F0<Object> supplier) {
        PK_PROVIDER.val = supplier;
    }

    /**
     * Static builder for subclasses
     * Entity is record with its primary key set;
     *
     * @param recordClass sub-class
     * @param <T>         sub-class-type
     * @return new entity
     */
    @SuppressWarnings("unchecked")
    static <T> T newEntity(Class<T> recordClass) {
        Record t = (Record) newValue(recordClass);
        t.pk(PK_PROVIDER.val.apply());
        return (T) t;
    }

    /**
     * Static builder for subclasses
     * Value is record without primary key set;
     *
     * @param recordClass sub-class
     * @param <T>         sub-class-type
     * @return new value OR throw exception when error occurred
     */
    static <T> T newValue(Class<T> recordClass) {
        try {
            return S._one(recordClass);
        } catch (InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * declared fields that should be same as declared in db
     * @return
     */
    public Set<String> declaredFields() ;

    /**
     * get primary key -value
     *
     * @return value
     */
    public <E> E pk();

    /**
     * set primary key
     *
     * @param pk pk -value
     * @return this
     */
    public Record pk(Object pk);

    /**
     * get primary key -name
     *
     * @return this
     */
    public String primaryKeyName();

    /**
     * set primaryKeyName
     *
     * @param keyName primary key name
     */
    void primaryKeyName(String keyName);

    /**
     * get table -name
     *
     * @return this
     */
    public String table();

    /**
     * set table -name
     *
     * @param s table-name
     * @return this
     */
    public Record table(String s);

    /**
     * get value
     *
     * @param s   name
     * @param <E> return type
     * @return E-typed value if get or null
     */
    <E> E get(String s);

    /**
     * set value
     *
     * @param s   name
     * @param val value
     * @return this
     */
    Object set(String s, Object val);

    /**
     * of argument as defined in declaredFields,
     * WARN: this method change the state of current object
     * rather than return a new copy
     *
     * @param req input request
     * @return altered this
     */
    Record of(Request req);

    /**
     * of argument as defined in declaredFields,
     * WARN: this method change the state of current object
     * rather than return a new copy
     *
     * @param map input map
     * @return altered this
     */
    Record merge(Map<String,Object> map);

    /**
     * get defined RowMapper
     *
     * @param <E> mapper-to type
     * @return rowMapper
     */
    <E> RowMapper<E> mapper();

    /**
     * set rowMapper
     *
     * @param mapper mapper to-set
     * @param <E>    mapper-to type
     * @return this
     */
    <E> Record mapper(RowMapper<E> mapper);

    /**
     * quick save
     */
    default void save() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.add(this));
    }

    /**
     * quick update
     */
    default void update() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.upd(this));
    }

    /**
     * quick delete
     */
    default void delete() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.del(this));
    }

}
