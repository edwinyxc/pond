package com.shuimin.pond.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.common.f.Holder;

import java.sql.ResultSet;
import java.util.HashMap;
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
public interface Record {

    public interface Field<E> {

        /**
         * Returns the name of this field.
         */
        String name();

        /**
         * Set how the data is getting from the resultSet.
         * function(name,mapper)
         */
        Field<E> mapper(Function.F2<E, String, ResultSet> e);

        /**
         * @param init
         * @return
         */
        Field<E> init(Function.F0<E> init);

        /**
         * Used for
         */
        Field<E> merge(Function<E, ?> validator);

        /**
         * Used for view rendering
         */
        <V> Field<E> view(Function<V, E> view);

        /**
         * Used for sql generation.
         */
        <D> Field<E> db(Function<D, E> data);


    }

    Record init();

    /**
     * Try get the Field if not found
     * create new one.
     *
     * @param name field name
     * @return this
     */
    <E> Field<E> field(String name);


    final static String DEFAULT_PRI_KEY = "id";

    /**
     * default pk_provider
     */
    static Holder<Function.F0<Object>> PK_PROVIDER =
            new Holder<Function.F0<Object>>().init(S.uuid::vid);

//    static void primaryKeySupplier(Function.F0<Object> supplier) {
//        PK_PROVIDER.val = supplier;
//    }

    /**
     * Static builder for subclasses
     * Entity is record with its primary key set;
     *
     * @param recordClass sub-class
     * @param <T>         sub-class-type
     * @return new entity
     */
    @SuppressWarnings("unchecked")
    static <T extends Record> T newEntity(Class<T> recordClass) {
        Record t = (Record) newValue(recordClass);
        t.setId(PK_PROVIDER.val.apply());
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
    @SuppressWarnings("unchecked")
    static <T extends Record> T newValue(Class<T> recordClass) {
        try {
            T t = S._one(recordClass);
            return (T) t.init();
        } catch (InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * declared fields that should be same as declared in db
     *
     * @return
     */
    public Set<String> declaredFields();

    /**
     * Returns all fields
     *
     * @return
     */
    public Set<String> fields();

    /**
     * get primary key -value
     *
     * @return value
     */
    public <E> E id();

    /**
     * set primary key
     *
     * @param pk id -value
     * @return this
     */
    public Record setId(Object pk);

    /**
     * get primary key -name
     *
     * @return this
     */
    public String idName();

    /**
     * set id
     *
     * @param keyName primary key name
     */
    void id(String keyName);

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
     * view a value ( entity -> view )
     */
    <E> E view(String s);

    /**
     * db a value ( entity -> db )
     */
    <E> E db(String s);

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
    Record set(String s, Object val);

    /**
     * of argument as defined in declaredFields,
     * WARN: this method change the state of current object
     * rather than return a new copy
     *
     * @param map input map
     * @return altered this
     */
    Record merge(Map<String, Object> map);

    /**
     * get defined RowMapper
     *
     * @param <E> rs_mapper-to type
     * @return rowMapper
     */
    <E> Function<E, ResultSet> mapper();

    /**
     * set rowMapper
     *
     * @param mapper rs_mapper to-set
     * @param <E>    rs_mapper-to type
     * @return this
     */
    <E> Record mapper(Function<E, ResultSet> mapper);

    //AR -QUICK

    /**
     * Quick save
     */
    default void save() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.add(this));
    }

    /**
     * Quick update
     */
    default void update() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.upd(this));
    }


    /**
     * Quick create
     */
    static <E extends Record> E create(Class<E> clz) {
        E r = Record.newEntity(clz);
        DB.fire(t -> t.add(r));
        return r;

    }


    /**
     * Quick delete
     */
    default void delete() {
        DB.fire((tmpl) -> tmpl.del(this));
    }


    default Map<String, Object> view() {
        Map<String, Object> ret = new HashMap<>();
        for (String s : this.fields()) {
            ret.put(s, view(s));
        }
        return ret;
    }

    default Map<String, Object> db() {

        Map<String, Object> ret = new HashMap<>();
        for (String s : this.declaredFields()) {
            ret.put(s, db(s));
        }
        return ret;
    }


}
